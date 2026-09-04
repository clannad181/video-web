package com.cjym.videoweb.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import co.elastic.clients.elasticsearch.indices.analyze.AnalyzeToken;
import co.elastic.clients.elasticsearch.indices.get_mapping.IndexMappingRecord;
import co.elastic.clients.json.JsonData;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.constant.VideoConstant;
import com.cjym.videoweb.mapper.FollowMapper;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.mapper.VideoEpisodeMapper;
import com.cjym.videoweb.mapper.VideoMapper;
import com.cjym.videoweb.model.entity.Follow;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.entity.VideoEpisode;
import com.cjym.videoweb.model.es.UpUserDoc;
import com.cjym.videoweb.model.es.VideoDoc;
import com.cjym.videoweb.model.event.FollowChangedEvent;
import com.cjym.videoweb.model.vo.CategorySimpleVo;
import com.cjym.videoweb.model.vo.ClientVideoVo;
import com.cjym.videoweb.model.vo.UpUserVo;
import com.cjym.videoweb.model.vo.UserEpisodeStatsVo;
import com.cjym.videoweb.service.CategoryService;
import com.cjym.videoweb.service.EsSearchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Elasticsearch 聚合搜索实现（仅 elasticsearch.enabled=true 时注册）。
 * 搜索基于索引文档，标题/昵称等中文文本字段使用 IK 分词（写入 ik_max_word、查询 ik_smart）；
 * 数据通过业务埋点增量同步（refreshVideo/refreshUser），
 * 应用启动时索引缺失或分词映射版本不一致则自动全量重建，保证开箱即用
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "elasticsearch", name = "enabled", havingValue = "true")
public class EsSearchServiceImpl implements EsSearchService, ApplicationRunner {

    /** 视频索引名 */
    private static final String VIDEO_INDEX = "video_search";
    /** UP主索引名 */
    private static final String UP_INDEX = "up_search";
    /** 索引映射版本号（_meta.version）：分词映射变更时递增，触发启动自动重建 */
    private static final String MAPPING_VERSION = "4";
    /** 映射版本元数据键 */
    private static final String MAPPING_META_KEY = "version";
    /** IK 分词器：写入时最大粒度切分 */
    private static final String IK_ANALYZER = "ik_max_word";
    /** IK 分词器：查询时智能切分 */
    private static final String IK_SEARCH_ANALYZER = "ik_smart";

    @Resource
    private ElasticsearchClient client;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private VideoEpisodeMapper videoEpisodeMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private FollowMapper followMapper;

    @Resource
    private CategoryService categoryService;

    // ==================== 搜索 ====================

    @Override
    public IPage<UpUserVo> searchUp(String keyword, Integer page, Integer size, String sortType) {
        int pageNum = page != null && page > 0 ? page : 1;
        int pageSize = size != null && size > 0 ? size : 10;
        String kw = keyword == null ? "" : keyword.trim();
        // 排序：sortType 未传或非法值时按相关度得分降序（粉丝数兜底），
        // 传了则按白名单字段降序（fans-粉丝数 like-累计获赞 play-累计播放，得分作为次级排序）
        final String sortField;
        if (sortType != null && !sortType.isBlank()) {
            sortField = switch (sortType.trim().toLowerCase()) {
                case "like" -> "totalLikeCount";
                case "play" -> "totalPlayCount";
                default -> "fansCount";
            };
        } else {
            sortField = null;
        }
        List<SortOptions> sorts = sortField == null
                ? List.of(SortOptions.of(so -> so.score(sc -> sc.order(SortOrder.Desc))),
                        SortOptions.of(so -> so.field(f -> f.field("fansCount").order(SortOrder.Desc))))
                : List.of(SortOptions.of(so -> so.field(f -> f.field(sortField).order(SortOrder.Desc))),
                        SortOptions.of(so -> so.score(sc -> sc.order(SortOrder.Desc))));
        // 昵称走 IK 分词 match 查询，uid 为纯数字串保留通配符匹配
        if (!kw.isEmpty()) {
            logAnalyzedTokens(UP_INDEX, kw);
        }
        Query query = kw.isEmpty()
                ? MatchAllQuery.of(m -> m)._toQuery()
                : BoolQuery.of(b -> b.should(List.of(
                        MatchQuery.of(mq -> mq.field("nikeName").query(kw))._toQuery(),
                        WildcardQuery.of(w -> w.field("uid").wildcard("*" + kw + "*"))._toQuery())))
                ._toQuery();
        SearchResponse<UpUserDoc> response;
        try {
            response = client.search(s -> s
                    .index(UP_INDEX)
                    .query(query)
                    .from((pageNum - 1) * pageSize)
                    .size(pageSize)
                    .sort(sorts)
                    .trackTotalHits(t -> t.enabled(true)), UpUserDoc.class);
        } catch (IOException e) {
            throw new RuntimeException("ES UP主搜索失败", e);
        }
        List<UpUserVo> records = response.hits().hits().stream()
                .map(h -> toUpVo(h.source()))
                .collect(Collectors.toList());
        long total = response.hits().total() != null ? response.hits().total().value() : 0;
        Page<UpUserVo> result = new Page<>(pageNum, pageSize, total);
        result.setRecords(records);
        return result;
    }

    @Override
    public IPage<ClientVideoVo> searchVideo(String keyword, Integer page, Integer size, String sortType) {
        int pageNum = page != null && page > 0 ? page : 1;
        int pageSize = size != null && size > 0 ? size : 10;
        String kw = keyword == null ? "" : keyword.trim();
        // 排序：sortType 未传时按相关度得分降序（发布时间兜底），
        // 传了则按白名单字段降序（latest-发布时间 hot-播放量 favorite-收藏数，非法值降级发布时间，得分作为次级排序）
        final String sortField;
        if (sortType != null && !sortType.isBlank()) {
            sortField = switch (sortType.trim().toLowerCase()) {
                case "hot" -> "playCount";
                case "favorite" -> "favoriteCount";
                default -> "createTime";
            };
        } else {
            sortField = null;
        }
        List<SortOptions> sorts = sortField == null
                ? List.of(SortOptions.of(so -> so.score(sc -> sc.order(SortOrder.Desc))),
                        SortOptions.of(so -> so.field(f -> f.field("createTime").order(SortOrder.Desc))))
                : List.of(SortOptions.of(so -> so.field(f -> f.field(sortField).order(SortOrder.Desc))),
                        SortOptions.of(so -> so.score(sc -> sc.order(SortOrder.Desc))));
        // 标题走 IK 分词 match 查询（映射中已配置 search_analyzer=ik_smart）
        if (!kw.isEmpty()) {
            logAnalyzedTokens(VIDEO_INDEX, kw);
        }
        Query query = kw.isEmpty()
                ? MatchAllQuery.of(m -> m)._toQuery()
                : MatchQuery.of(mq -> mq.field("title").query(kw))._toQuery();
        SearchResponse<VideoDoc> response;
        try {
            response = client.search(s -> s
                    .index(VIDEO_INDEX)
                    .query(query)
                    .from((pageNum - 1) * pageSize)
                    .size(pageSize)
                    .sort(sorts)
                    .trackTotalHits(t -> t.enabled(true)), VideoDoc.class);
        } catch (IOException e) {
            throw new RuntimeException("ES 视频搜索失败", e);
        }
        List<ClientVideoVo> records = response.hits().hits().stream()
                .map(h -> toVideoVo(h.source()))
                .collect(Collectors.toList());
        long total = response.hits().total() != null ? response.hits().total().value() : 0;
        Page<ClientVideoVo> result = new Page<>(pageNum, pageSize, total);
        result.setRecords(records);
        return result;
    }

    @Override
    public IPage<ClientVideoVo> listVideo(Integer page, Integer size, String sortType, Long categoryId) {
        // 查询：全部已入库视频（索引内均为已发布且至少一集审核通过），可选按分类过滤
        Query query = categoryId != null
                ? TermQuery.of(t -> t.field("categoryId").value(categoryId))._toQuery()
                : MatchAllQuery.of(m -> m)._toQuery();
        return listVideoByQuery(page, size, sortType, query);
    }

    @Override
    public IPage<ClientVideoVo> listUserVideos(Long userId, Integer page, Integer size, String sortType) {
        // 查询：仅指定UP主的已入库视频
        Query query = TermQuery.of(t -> t.field("userId").value(userId))._toQuery();
        return listVideoByQuery(page, size, sortType, query);
    }

    /**
     * 前台视频列表通用查询：按 latest（发布时间）/hot（播放量）倒序分页
     * （浏览场景无关键词匹配，无相关度得分，非法排序值降级发布时间）
     */
    private IPage<ClientVideoVo> listVideoByQuery(Integer page, Integer size, String sortType, Query query) {
        int pageNum = page != null && page > 0 ? page : 1;
        int pageSize = size != null && size > 0 ? size : 10;
        // 排序白名单：latest-发布时间 hot-播放量，未传或非法值降级发布时间，均倒序
        final String sortField;
        if (sortType != null && !sortType.isBlank()) {
            sortField = switch (sortType.trim().toLowerCase()) {
                case "hot" -> "playCount";
                default -> "createTime";
            };
        } else {
            sortField = "createTime";
        }
        List<SortOptions> sorts = List.of(
                SortOptions.of(so -> so.field(f -> f.field(sortField).order(SortOrder.Desc))),
                SortOptions.of(so -> so.field(f -> f.field("createTime").order(SortOrder.Desc))));
        SearchResponse<VideoDoc> response;
        try {
            response = client.search(s -> s
                    .index(VIDEO_INDEX)
                    .query(query)
                    .from((pageNum - 1) * pageSize)
                    .size(pageSize)
                    .sort(sorts)
                    .trackTotalHits(t -> t.enabled(true)), VideoDoc.class);
        } catch (IOException e) {
            throw new RuntimeException("ES 视频列表查询失败", e);
        }
        List<ClientVideoVo> records = response.hits().hits().stream()
                .map(h -> toVideoVo(h.source()))
                .collect(Collectors.toList());
        long total = response.hits().total() != null ? response.hits().total().value() : 0;
        Page<ClientVideoVo> result = new Page<>(pageNum, pageSize, total);
        result.setRecords(records);
        return result;
    }

    // ==================== 索引同步 ====================

    @Override
    public void refreshVideo(Long videoId) {
        if (videoId == null) {
            return;
        }
        // 视频已删除（逻辑删除过滤后查不到）或未发布：移除文档
        Video video = videoMapper.selectById(videoId);
        if (video == null || !Objects.equals(video.getVideoStatus(), VideoConstant.VIDEO_STATUS_PUBLISHED)) {
            deleteDocument(VIDEO_INDEX, videoId);
            return;
        }
        // 聚合剧集统计：播放/收藏累加、最新审核通过剧集avid
        List<VideoEpisode> episodes = videoEpisodeMapper.selectList(new LambdaQueryWrapper<VideoEpisode>()
                .eq(VideoEpisode::getVideoId, videoId));
        if (episodes.isEmpty()) {
            deleteDocument(VIDEO_INDEX, videoId);
            return;
        }
        long playCount = episodes.stream().mapToLong(e -> e.getPlayCount() != null ? e.getPlayCount() : 0L).sum();
        long favoriteCount = episodes.stream().mapToLong(e -> e.getFavoriteCount() != null ? e.getFavoriteCount() : 0L).sum();
        String episodeAvid = episodes.stream()
                .filter(e -> e.getReviewStatus() != null && e.getReviewStatus() == VideoConstant.REVIEW_APPROVED)
                .max(Comparator.comparingInt(e -> e.getEpisodeNumber() != null ? e.getEpisodeNumber() : 0))
                .map(VideoEpisode::getAvid)
                .orElse(null);
        // 无审核通过剧集：不符合搜索条件，移除文档
        if (episodeAvid == null) {
            deleteDocument(VIDEO_INDEX, videoId);
            return;
        }
        // 分类名称
        String categoryName = null;
        if (video.getCategoryId() != null) {
            categoryName = categoryService.listEnabled().stream()
                    .filter(c -> Objects.equals(c.getId(), video.getCategoryId()))
                    .map(CategorySimpleVo::getName)
                    .findFirst().orElse(null);
        }
        // UP主信息
        UserInfo user = video.getUserId() != null ? userInfoMapper.selectById(video.getUserId()) : null;
        // 组装文档并写入
        VideoDoc doc = new VideoDoc();
        doc.setId(video.getId());
        doc.setAvid(video.getAvid());
        doc.setTitle(video.getTitle());
        doc.setCoverUrl(video.getCoverUrl());
        doc.setTotalDuration(video.getTotalDuration());
        doc.setEpisodeCount(video.getEpisodeCount());
        doc.setPlayCount(playCount);
        doc.setFavoriteCount(favoriteCount);
        doc.setEpisodeAvid(episodeAvid);
        doc.setIsOngoing(video.getIsOngoing());
        doc.setCreateTime(video.getCreateTime() != null
                ? video.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null);
        doc.setUserId(video.getUserId());
        doc.setCategoryId(video.getCategoryId());
        doc.setCategoryName(categoryName);
        doc.setNickName(user != null ? user.getNikeName() : null);
        doc.setAvatarUrl(user != null ? user.getAvatar() : null);
        doc.setUid(user != null ? user.getUid() : null);
        indexDocument(VIDEO_INDEX, videoId, doc);
    }

    @Override
    public void deleteVideo(Long videoId) {
        if (videoId != null) {
            deleteDocument(VIDEO_INDEX, videoId);
        }
    }

    @Override
    public void refreshUser(Long userId) {
        if (userId == null) {
            return;
        }
        UserInfo user = userInfoMapper.selectById(userId);
        if (user == null) {
            deleteDocument(UP_INDEX, userId);
            return;
        }
        // 聚合统计：关注数/粉丝数/累计获赞/累计播放
        UserEpisodeStatsVo stats = videoMapper.selectUserEpisodeStats(userId);
        long followCount = followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getUserId, userId));
        long fansCount = followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowUserId, userId));
        UpUserDoc doc = new UpUserDoc();
        doc.setId(user.getId());
        doc.setUid(user.getUid());
        doc.setNikeName(user.getNikeName());
        doc.setAvatar(user.getAvatar());
        doc.setDescript(user.getDescript());
        doc.setSex(user.getSex());
        doc.setIsVip(user.getIsVip());
        doc.setFollowCount(followCount);
        doc.setFansCount(fansCount);
        doc.setTotalLikeCount(stats != null && stats.getTotalLikeCount() != null ? stats.getTotalLikeCount() : 0L);
        doc.setTotalPlayCount(stats != null && stats.getTotalPlayCount() != null ? stats.getTotalPlayCount() : 0L);
        indexDocument(UP_INDEX, userId, doc);
    }

    /**
     * 关注关系变更事件监听：同步关注者与被关注者的 ES 文档（关注数/粉丝数变化）。
     * 该监听器注册在 EsSearchServiceImpl 上，ES 未启用时类不装配，事件自然无人处理
     */
    @EventListener
    public void onFollowChanged(FollowChangedEvent event) {
        try {
            refreshUser(event.getUserId());
            refreshUser(event.getFollowUserId());
        } catch (Exception e) {
            log.warn("关注关系变更同步 ES 失败 userId={} followUserId={}: {}",
                    event.getUserId(), event.getFollowUserId(), e.getMessage());
        }
    }

    @Override
    public void rebuildAll() throws IOException {
        log.info("开始全量重建 ES 聚合搜索索引");
        // 1. 删除旧索引（不存在则忽略），按 IK 分词映射重建
        deleteIndexIfExists(VIDEO_INDEX);
        deleteIndexIfExists(UP_INDEX);
        createVideoIndex();
        createUpIndex();
        // 2. 全量同步视频（复用数据库搜索查询分页拉取，仅已发布且至少有一集审核通过）
        long pageNum = 1;
        while (true) {
            IPage<ClientVideoVo> pageResult = videoMapper.selectSearchVideoPage(
                    new Page<>(pageNum, 500), null, "v.create_time");
            for (ClientVideoVo vo : pageResult.getRecords()) {
                indexDocument(VIDEO_INDEX, vo.getId(), toVideoDoc(vo));
            }
            if (pageNum * 500 >= pageResult.getTotal()) {
                break;
            }
            pageNum++;
        }
        // 3. 全量同步用户
        pageNum = 1;
        while (true) {
            IPage<UpUserVo> pageResult = userInfoMapper.selectSearchUpPage(
                    new Page<>(pageNum, 500), null, "fans_count");
            for (UpUserVo vo : pageResult.getRecords()) {
                Long userId = findUserIdByUid(vo.getUid());
                if (userId != null) {
                    indexDocument(UP_INDEX, userId, toUpUserDoc(vo));
                }
            }
            if (pageNum * 500 >= pageResult.getTotal()) {
                break;
            }
            pageNum++;
        }
        log.info("ES 聚合搜索索引全量重建完成");
    }

    // ==================== 启动检查 ====================

    @Override
    public void run(ApplicationArguments args) {
        try {
            boolean videoIndexOk = isMappingCurrent(VIDEO_INDEX);
            boolean upIndexOk = isMappingCurrent(UP_INDEX);
            if (!videoIndexOk || !upIndexOk) {
                log.info("检测到 ES 索引缺失或分词映射版本不一致（video_search={}, up_search={}），开始自动全量重建", videoIndexOk, upIndexOk);
                rebuildAll();
                log.info("ES 索引全量重建完成");
            }
        } catch (Exception e) {
            log.warn("ES 启动检查失败（可忽略，聚合搜索将降级数据库查询）: {}", e.getMessage());
        }
    }

    // ==================== 私有工具方法 ====================

    /**
     * 记录关键字经 IK 分词器的分词结果（诊断日志，便于排查搜索命中问题）；
     * 同时输出查询分词（ik_smart）与写入分词（ik_max_word）两种结果，分析接口异常时静默忽略
     */
    private void logAnalyzedTokens(String indexName, String keyword) {
        try {
            List<String> smartTokens = analyzeTokens(indexName, IK_SEARCH_ANALYZER, keyword);
            List<String> maxWordTokens = analyzeTokens(indexName, IK_ANALYZER, keyword);
//            log.info("ES 分词结果 index={} keyword={} ik_smart={} ik_max_word={}",
//                    indexName, keyword, smartTokens, maxWordTokens);
        } catch (IOException e) {
            log.warn("ES 分词分析失败 index={} keyword={}: {}", indexName, keyword, e.getMessage());
        }
    }

    /**
     * 调用 ES Analyze API 按指定分词器切分文本，返回词元列表
     */
    private List<String> analyzeTokens(String indexName, String analyzer, String text) throws IOException {
        AnalyzeResponse response = client.indices().analyze(a -> a
                .index(indexName)
                .analyzer(analyzer)
                .text(text));
        return response.tokens().stream()
                .map(AnalyzeToken::token)
                .collect(Collectors.toList());
    }

    /**
     * 索引文档（索引缺失时先按 IK 分词映射创建再写入）
     */
    private void indexDocument(String indexName, Long docId, Object doc) {
        if (docId == null) {
            return;
        }
        ensureIndexCreated(indexName);
        try {
            client.index(i -> i.index(indexName).id(String.valueOf(docId)).document(doc));
        } catch (IOException e) {
            throw new RuntimeException("ES 索引文档失败 index=" + indexName + " id=" + docId, e);
        }
    }

    /**
     * 删除文档（文档不存在或删除失败时忽略）
     */
    private void deleteDocument(String indexName, Long docId) {
        try {
            client.delete(d -> d.index(indexName).id(String.valueOf(docId)));
        } catch (ElasticsearchException e) {
            if (e.status() != 404) {
                log.warn("删除 ES 文档失败 index={} id={}: {}", indexName, docId, e.getMessage());
            }
        } catch (IOException e) {
            log.warn("删除 ES 文档失败 index={} id={}: {}", indexName, docId, e.getMessage());
        }
    }

    /**
     * 删除索引（不存在时忽略）
     */
    private void deleteIndexIfExists(String indexName) {
        try {
            if (client.indices().exists(e -> e.index(indexName)).value()) {
                client.indices().delete(d -> d.index(indexName));
            }
        } catch (Exception e) {
            log.warn("删除 ES 索引 {} 失败: {}", indexName, e.getMessage());
        }
    }

    /**
     * 创建视频搜索索引：标题/UP主昵称等中文文本字段使用 IK 分词（写入 ik_max_word 全量切分、查询 ik_smart 智能切分），
     * 其余字段按类型显式映射，并通过 _meta.version 记录映射版本
     */
    private void createVideoIndex() throws IOException {
        client.indices().create(c -> c.index(VIDEO_INDEX)
                .mappings(m -> m
                        .properties("id", p -> p.long_(f -> f))
                        .properties("avid", p -> p.keyword(k -> k))
                        .properties("title", p -> p.text(t -> t.analyzer(IK_ANALYZER).searchAnalyzer(IK_SEARCH_ANALYZER)))
                        .properties("coverUrl", p -> p.keyword(k -> k))
                        .properties("totalDuration", p -> p.long_(f -> f))
                        .properties("episodeCount", p -> p.integer(i -> i))
                        .properties("playCount", p -> p.long_(f -> f))
                        .properties("favoriteCount", p -> p.long_(f -> f))
                        .properties("episodeAvid", p -> p.keyword(k -> k))
                        .properties("isOngoing", p -> p.boolean_(b -> b))
                        .properties("createTime", p -> p.long_(f -> f))
                        .properties("userId", p -> p.long_(f -> f))
                        .properties("categoryId", p -> p.long_(f -> f))
                        .properties("categoryName", p -> p.keyword(k -> k))
                        .properties("nickName", p -> p.text(t -> t.analyzer(IK_ANALYZER).searchAnalyzer(IK_SEARCH_ANALYZER)))
                        .properties("avatarUrl", p -> p.keyword(k -> k))
                        .properties("uid", p -> p.keyword(k -> k))
                        .meta(Map.of(MAPPING_META_KEY, JsonData.of(MAPPING_VERSION)))));
    }

    /**
     * 创建UP主搜索索引：昵称/个性签名使用 IK 分词，uid 使用 keyword（支持通配符匹配）；
     * 关注数/粉丝数不在 ES 维护，搜索结果由数据库实时统计填充
     */
    private void createUpIndex() throws IOException {
        client.indices().create(c -> c.index(UP_INDEX)
                .mappings(m -> m
                        .properties("id", p -> p.long_(f -> f))
                        .properties("uid", p -> p.keyword(k -> k))
                        .properties("nikeName", p -> p.text(t -> t.analyzer(IK_ANALYZER).searchAnalyzer(IK_SEARCH_ANALYZER)))
                        .properties("avatar", p -> p.keyword(k -> k))
                        .properties("descript", p -> p.text(t -> t.analyzer(IK_ANALYZER).searchAnalyzer(IK_SEARCH_ANALYZER)))
                        .properties("sex", p -> p.boolean_(b -> b))
                        .properties("isVip", p -> p.boolean_(b -> b))
                        .properties("followCount", p -> p.long_(f -> f))
                        .properties("fansCount", p -> p.long_(f -> f))
                        .properties("totalLikeCount", p -> p.long_(f -> f))
                        .properties("totalPlayCount", p -> p.long_(f -> f))
                        .meta(Map.of(MAPPING_META_KEY, JsonData.of(MAPPING_VERSION)))));
    }

    /**
     * 确保索引已按 IK 分词映射创建：索引缺失时补建，
     * 防止运行时索引被删除后由动态映射自动重建而丢失分词器配置
     */
    private void ensureIndexCreated(String indexName) {
        try {
            if (client.indices().exists(e -> e.index(indexName)).value()) {
                return;
            }
            if (VIDEO_INDEX.equals(indexName)) {
                createVideoIndex();
            } else {
                createUpIndex();
            }
        } catch (IOException e) {
            log.warn("创建 ES 索引 {} 失败: {}", indexName, e.getMessage());
        }
    }

    /**
     * 索引映射是否与当前版本一致：索引缺失、无版本元数据（旧版动态映射）或版本不符时返回 false，
     * 由启动检查触发全量重建
     */
    private boolean isMappingCurrent(String indexName) {
        try {
            if (!client.indices().exists(e -> e.index(indexName)).value()) {
                return false;
            }
            GetMappingResponse mapping = client.indices().getMapping(g -> g.index(indexName));
            IndexMappingRecord record = mapping.get(indexName);
            if (record == null || record.mappings() == null || record.mappings().meta() == null) {
                return false;
            }
            JsonData version = record.mappings().meta().get(MAPPING_META_KEY);
            return version != null && MAPPING_VERSION.equals(version.to(String.class));
        } catch (IOException e) {
            log.warn("检查 ES 索引 {} 映射版本失败: {}", indexName, e.getMessage());
            return false;
        }
    }

    /**
     * 数据库查询结果 ClientVideoVo → ES 文档（全量重建使用）
     */
    private VideoDoc toVideoDoc(ClientVideoVo vo) {
        VideoDoc doc = new VideoDoc();
        doc.setId(vo.getId());
        doc.setAvid(vo.getAvid());
        doc.setTitle(vo.getTitle());
        doc.setCoverUrl(vo.getCoverUrl());
        doc.setTotalDuration(vo.getTotalDuration());
        doc.setEpisodeCount(vo.getEpisodeCount());
        doc.setPlayCount(vo.getPlayCount());
        doc.setFavoriteCount(vo.getFavoriteCount());
        doc.setEpisodeAvid(vo.getEpisodeAvid());
        doc.setIsOngoing(vo.getIsOngoing());
        doc.setCreateTime(vo.getCreateTime() != null
                ? vo.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null);
        doc.setUserId(vo.getUserId());
        doc.setCategoryId(vo.getCategoryId());
        doc.setCategoryName(vo.getCategoryName());
        doc.setNickName(vo.getNickName());
        doc.setAvatarUrl(vo.getAvatarUrl());
        doc.setUid(vo.getUid());
        return doc;
    }

    /**
     * 数据库查询结果 UpUserVo → ES 文档（全量重建使用）
     */
    private UpUserDoc toUpUserDoc(UpUserVo vo) {
        UpUserDoc doc = new UpUserDoc();
        doc.setUid(vo.getUid());
        doc.setNikeName(vo.getNikeName());
        doc.setAvatar(vo.getAvatar());
        doc.setDescript(vo.getDescript());
        doc.setSex(vo.getSex());
        doc.setIsVip(vo.getIsVip());
        doc.setFollowCount(vo.getFollowCount());
        doc.setFansCount(vo.getFansCount());
        doc.setTotalLikeCount(vo.getTotalLikeCount());
        doc.setTotalPlayCount(vo.getTotalPlayCount());
        return doc;
    }

    /**
     * 按 uid 反查用户id（全量重建时补文档id）
     */
    private Long findUserIdByUid(String uid) {
        if (uid == null) {
            return null;
        }
        UserInfo user = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUid, uid));
        return user != null ? user.getId() : null;
    }

    /**
     * ES 文档 → UpUserVo（isFollowed 由调用方填充）
     */
    private UpUserVo toUpVo(UpUserDoc doc) {
        UpUserVo vo = new UpUserVo();
        vo.setUid(doc.getUid());
        vo.setNikeName(doc.getNikeName());
        vo.setAvatar(doc.getAvatar());
        vo.setDescript(doc.getDescript());
        vo.setSex(doc.getSex());
        vo.setIsVip(doc.getIsVip());
        vo.setFollowCount(doc.getFollowCount());
        vo.setFansCount(doc.getFansCount());
        vo.setTotalLikeCount(doc.getTotalLikeCount());
        vo.setTotalPlayCount(doc.getTotalPlayCount());
        return vo;
    }

    /**
     * ES 文档 → ClientVideoVo（playLink 与 isFollowed 由调用方统一填充）
     */
    private ClientVideoVo toVideoVo(VideoDoc doc) {
        ClientVideoVo vo = new ClientVideoVo();
        vo.setId(doc.getId());
        vo.setAvid(doc.getAvid());
        vo.setEpisodeAvid(doc.getEpisodeAvid());
        vo.setTitle(doc.getTitle());
        vo.setCoverUrl(doc.getCoverUrl());
        vo.setTotalDuration(doc.getTotalDuration());
        vo.setEpisodeCount(doc.getEpisodeCount());
        vo.setPlayCount(doc.getPlayCount());
        vo.setFavoriteCount(doc.getFavoriteCount());
        vo.setCategoryId(doc.getCategoryId());
        vo.setCategoryName(doc.getCategoryName());
        vo.setNickName(doc.getNickName());
        vo.setUserId(doc.getUserId());
        vo.setAvatarUrl(doc.getAvatarUrl());
        vo.setUid(doc.getUid());
        vo.setIsOngoing(doc.getIsOngoing());
        if (doc.getCreateTime() != null) {
            vo.setCreateTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(doc.getCreateTime()), ZoneId.systemDefault()));
        }
        return vo;
    }
}
