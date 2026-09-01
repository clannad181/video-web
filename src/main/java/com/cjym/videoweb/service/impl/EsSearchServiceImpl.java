package com.cjym.videoweb.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Elasticsearch 聚合搜索实现（仅 elasticsearch.enabled=true 时注册）。
 * 搜索基于索引文档，数据通过业务埋点增量同步（refreshVideo/refreshUser）；
 * 应用启动时索引缺失则自动全量重建，保证开箱即用
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "elasticsearch", name = "enabled", havingValue = "true")
public class EsSearchServiceImpl implements EsSearchService, ApplicationRunner {

    /** 视频索引名 */
    private static final String VIDEO_INDEX = "video_search";
    /** UP主索引名 */
    private static final String UP_INDEX = "up_search";

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
        // 排序白名单：fans-粉丝数 like-累计获赞 play-累计播放，非法值降级粉丝数
        String sortField = switch (sortType == null || sortType.isBlank() ? "fans" : sortType.trim().toLowerCase()) {
            case "like" -> "totalLikeCount";
            case "play" -> "totalPlayCount";
            default -> "fansCount";
        };
        Query query = kw.isEmpty()
                ? MatchAllQuery.of(m -> m)._toQuery()
                : BoolQuery.of(b -> b.should(List.of(
                        WildcardQuery.of(w -> w.field("nikeName.keyword").wildcard("*" + kw + "*"))._toQuery(),
                        WildcardQuery.of(w -> w.field("uid.keyword").wildcard("*" + kw + "*"))._toQuery())))
                ._toQuery();
        SearchResponse<UpUserDoc> response;
        try {
            response = client.search(s -> s
                    .index(UP_INDEX)
                    .query(query)
                    .from((pageNum - 1) * pageSize)
                    .size(pageSize)
                    .sort(so -> so.field(f -> f.field(sortField).order(SortOrder.Desc)))
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
        // 排序白名单：latest-发布时间 hot-播放量 favorite-收藏数，非法值降级发布时间
        String sortField = switch (sortType == null || sortType.isBlank() ? "latest" : sortType.trim().toLowerCase()) {
            case "hot" -> "playCount";
            case "favorite" -> "favoriteCount";
            default -> "createTime";
        };
        Query query = kw.isEmpty()
                ? MatchAllQuery.of(m -> m)._toQuery()
                : WildcardQuery.of(w -> w.field("title.keyword").wildcard("*" + kw + "*"))._toQuery();
        SearchResponse<VideoDoc> response;
        try {
            response = client.search(s -> s
                    .index(VIDEO_INDEX)
                    .query(query)
                    .from((pageNum - 1) * pageSize)
                    .size(pageSize)
                    .sort(so -> so.field(f -> f.field(sortField).order(SortOrder.Desc)))
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
        // 关注数/粉丝数统计
        Long followCount = followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getUserId, userId));
        Long fansCount = followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowUserId, userId));
        // 累计获赞/累计播放聚合（复用现有统计查询）
        UserEpisodeStatsVo stats = videoMapper.selectUserEpisodeStats(userId);
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

    @Override
    public void rebuildAll() {
        log.info("开始全量重建 ES 聚合搜索索引");
        // 1. 删除旧索引（不存在则忽略），重新写入时自动创建
        deleteIndexIfExists(VIDEO_INDEX);
        deleteIndexIfExists(UP_INDEX);
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
            boolean videoIndexExists = client.indices().exists(e -> e.index(VIDEO_INDEX)).value();
            boolean upIndexExists = client.indices().exists(e -> e.index(UP_INDEX)).value();
            if (!videoIndexExists || !upIndexExists) {
                log.info("检测到 ES 索引缺失（video_search={}, up_search={}），开始自动全量重建", videoIndexExists, upIndexExists);
                rebuildAll();
                log.info("ES 索引全量重建完成");
            }
        } catch (Exception e) {
            log.warn("ES 启动检查失败（可忽略，聚合搜索将降级数据库查询）: {}", e.getMessage());
        }
    }

    // ==================== 私有工具方法 ====================

    /**
     * 索引文档（索引不存在时 ES 自动创建并使用动态映射）
     */
    private void indexDocument(String indexName, Long docId, Object doc) {
        if (docId == null) {
            return;
        }
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
     * ES 文档 → UpUserVo
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
