package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.FollowMapper;
import com.cjym.videoweb.mapper.VideoEpisodeMapper;
import com.cjym.videoweb.mapper.VideoMapper;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.constant.BaseConstant;
import com.cjym.videoweb.constant.VideoConstant;
import com.cjym.videoweb.model.dto.EpisodePageDto;
import com.cjym.videoweb.model.dto.EpisodeUpdateDto;
import com.cjym.videoweb.model.dto.VideoContributeDto;
import com.cjym.videoweb.model.dto.VideoEpisodeAddDto;
import com.cjym.videoweb.model.dto.VideoEpisodeSaveBatchDto;
import com.cjym.videoweb.model.dto.VideoEpisodeSaveDto;
import com.cjym.videoweb.model.dto.VideoPageDto;
import com.cjym.videoweb.model.dto.VideoSaveDto;
import com.cjym.videoweb.model.entity.Follow;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.entity.VideoEpisode;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.model.vo.CategorySimpleVo;
import com.cjym.videoweb.model.vo.CategoryStatsVo;
import com.cjym.videoweb.model.vo.ClientVideoDetailVo;
import com.cjym.videoweb.model.vo.ClientVideoVo;
import com.cjym.videoweb.model.vo.DataCenterVo;
import com.cjym.videoweb.model.vo.MonthlyContributionVo;
import com.cjym.videoweb.model.vo.MyContributionVideoVo;
import com.cjym.videoweb.model.vo.VideoEpisodeVo;
import com.cjym.videoweb.model.vo.DailyIncrementVo;
import com.cjym.videoweb.model.vo.VideoReviewStatsVo;
import com.cjym.videoweb.model.vo.VideoVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.CategoryService;
import com.cjym.videoweb.service.CommentService;
import com.cjym.videoweb.service.EsSearchService;
import com.cjym.videoweb.service.VideoService;
import com.cjym.videoweb.service.WatchHistoryService;
import com.cjym.videoweb.service.WebConfigService;
import com.cjym.videoweb.utils.Base62Converter;
import com.cjym.videoweb.utils.JWTUtil;
import com.cjym.videoweb.utils.SnowflakeIdGenerator;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    @Resource
    private VideoEpisodeMapper videoEpisodeMapper;

    @Resource
    private WebConfigService webConfigService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private FollowMapper followMapper;

    @Resource
    private WatchHistoryService watchHistoryService;

    @Resource
    private CommentService commentService;

    /**
     * 聚合搜索门面：按开关在 ES 与数据库适配器间路由，ES 异常自动降级数据库
     */
    @Resource
    private SearchServiceFacade searchServiceFacade;

    /**
     * ES 索引同步服务：仅 elasticsearch.enabled=true 时存在，未启用时为 null（埋点静默跳过）
     */
    @Resource
    private ObjectProvider<EsSearchService> esSearchServiceProvider;

    /**
     * 排序字段编码 → SELECT 别名 映射（1:总时长 2:播放量 3:收藏数 4:创建时间）
     */
    private static final Map<Integer, String> FIELD_COLUMN_MAP = Map.of(
            1, "total_duration",
            2, "play_count",
            3, "favorite_count",
            4, "create_time"
    );

    private static final Set<String> ALLOWED_DIRECTIONS = Set.of("ASC", "DESC");

    @Override
    public IPage<VideoPageDto> pageVideo(Integer page, Integer size, String title,
                                         Long categoryId, Integer videoStates, String nickName, String uid,
                                         Integer field, String descOrAsc, Boolean isOngoing, String avid) {
        Page<VideoPageDto> pageParam = new Page<>(page, size);
        String orderColumn = resolveColumn(field);
        String orderDirection = resolveDirection(descOrAsc);
        return baseMapper.selectVideoPage(pageParam, title, categoryId, videoStates, nickName, uid,
                orderColumn, orderDirection,isOngoing,avid);
    }

    @Override
    public IPage<ClientVideoVo> pageClientVideo(Integer page, Integer size, String sortType, Long categoryId) {
        // 搜索实现（ES/数据库适配器）由 SearchServiceFacade 按开关路由，ES 异常自动降级数据库
        IPage<ClientVideoVo> result = searchServiceFacade.listVideo(page, size, sortType, categoryId);
        // 填充当前用户是否已关注UP主的标记
        fillFollowStatus(result.getRecords());
        // 填充播放跳转链接
        fillPlayLink(result.getRecords());
        return result;
    }

    @Override
    public IPage<ClientVideoVo> pageUserVideos(String userUid, Integer page, Integer size, String sortType) {
        if (userUid == null || userUid.isBlank()) {
            throw new GlobeException("UP主uid不能为空");
        }
        // 按 uid 反查 UP主id（对外接口参数不使用数据库主键）
        UserInfo up = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUid, userUid.trim()));
        if (up == null) {
            throw new GlobeException("UP主不存在");
        }
        // 搜索实现（ES/数据库适配器）由 SearchServiceFacade 按开关路由，ES 异常自动降级数据库
        IPage<ClientVideoVo> result = searchServiceFacade.listUserVideos(up.getId(), page, size, sortType);
        // 填充当前用户是否已关注UP主的标记
        fillFollowStatus(result.getRecords());
        // 填充播放跳转链接
        fillPlayLink(result.getRecords());
        return result;
    }

    @Override
    public List<ClientVideoVo> recommend() {
        List<ClientVideoVo> result;
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            // 未登录：返回最多收藏的视频
            result = baseMapper.selectFavoriteTop(BaseConstant.VIDEO_RECOMMEND_SIZE, null);
        } else {
            // 已登录：返回所有关注UP主的最新视频（最多6条）
            List<ClientVideoVo> follows = baseMapper.selectFollowLatest(currentUserId, BaseConstant.VIDEO_RECOMMEND_SIZE);
            if (follows.size() < BaseConstant.VIDEO_RECOMMEND_SIZE) {
                // 关注视频不足6条（含关注数为0）时用最多收藏的视频补齐（排除已推荐视频）
                List<Long> excludeIds = follows.stream().map(ClientVideoVo::getId).collect(Collectors.toList());
                List<ClientVideoVo> hot = baseMapper.selectFavoriteTop(BaseConstant.VIDEO_RECOMMEND_SIZE - follows.size(), excludeIds);
                follows.addAll(hot);
            }
            result = follows;
        }
        // 填充当前用户是否已关注UP主的标记
        fillFollowStatus(result);
        // 填充播放跳转链接
        fillPlayLink(result);
        return result;
    }

    @Override
    public IPage<ClientVideoVo> pageRelatedVideos(Integer page, Integer size, String videoUid) {
        if (videoUid == null || videoUid.isBlank()) {
            throw new GlobeException("视频avid不能为空");
        }
        // 第一步：按 avid 查询当前视频（拿 UP主id 和标签）
        Video video = baseMapper.selectOne(new LambdaQueryWrapper<Video>().eq(Video::getAvid, videoUid));
        if (video == null) {
            throw new GlobeException("视频不存在");
        }
        // 第二步：查询候选视频id列表（同UP主或标签有交集，标签取自数据库查询到的当前视频tags，排除自身，仅已发布且至少一集审核通过）
        List<Long> candidateIds = baseMapper.selectRelatedVideoIds(video.getId(), video.getUserId(),
                video.getTags(), BaseConstant.RELATED_VIDEO_CANDIDATE_LIMIT);
        Page<ClientVideoVo> result = new Page<>(page, size);
        if (candidateIds.isEmpty()) {
            result.setTotal(0);
            return result;
        }
        // 第三步：按候选id批量查询前台视频信息
        List<ClientVideoVo> records = baseMapper.selectClientVideoByIds(candidateIds);
        // 第四步：内存排序（同UP主优先，再按播放量降序）
        Long upUserId = video.getUserId();
        records.sort(Comparator
                .comparing((ClientVideoVo vo) -> !upUserId.equals(vo.getUserId()))
                .thenComparing(vo -> vo.getPlayCount() == null ? 0L : vo.getPlayCount(),
                        Comparator.reverseOrder()));
        // 第五步：内存分页（避免分页插件对复杂SQL做count优化）
        long total = records.size();
        int fromIndex = (int) Math.min((long) (page - 1) * size, total);
        int toIndex = (int) Math.min((long) fromIndex + size, total);
        result.setRecords(records.subList(fromIndex, toIndex));
        result.setTotal(total);
        // 填充播放跳转链接与关注标记
        fillPlayLink(result.getRecords());
        fillFollowStatus(result.getRecords());
        return result;
    }

    /**
     * 为前台视频记录填充播放跳转链接：/{videoAvid}/episode/{episodeAvid}
     * <p>
     * episodeAvid 取自审核通过剧集中集数序号最大（最新更新）的一集，缺失时链接不填充
     */
    private void fillPlayLink(List<ClientVideoVo> vos) {
        vos.forEach(vo -> {
            if (vo.getAvid() != null && vo.getEpisodeAvid() != null) {
                vo.setPlayLink("/client/video/" + vo.getAvid() + "/episode/" + vo.getEpisodeAvid());
            }
        });
    }

    @Override
    public ClientVideoDetailVo getClientPlayDetail(String videoUid, String episodeUid) {
        if (videoUid == null || videoUid.isBlank() || episodeUid == null || episodeUid.isBlank()) {
            throw new GlobeException("视频avid和剧集avid不能为空");
        }
        // JOIN user_info 获取UP主最新信息
        VideoVo videoVo = baseMapper.selectDetailByAvid(videoUid);
        if (videoVo == null) {
            throw new GlobeException("视频不存在");
        }
        // 未发布视频仅作者本人可预览（普通用户不可播放）
        Long currentUserId = getCurrentUserId();
        boolean isOwner = currentUserId != null && currentUserId.equals(videoVo.getUserId());
        boolean published = videoVo.getVideoStatus() != null
                && videoVo.getVideoStatus() == VideoConstant.VIDEO_STATUS_PUBLISHED;
        if (!published && !isOwner) {
            throw new GlobeException("视频未发布，无法播放");
        }
        // 剧集查询：普通用户仅展示审核通过的剧集，作者预览可见全部剧集
        LambdaQueryWrapper<VideoEpisode> epWrapper = new LambdaQueryWrapper<>();
        epWrapper.eq(VideoEpisode::getVideoId, videoVo.getId());
        if (!isOwner) {
            epWrapper.eq(VideoEpisode::getReviewStatus, VideoConstant.REVIEW_APPROVED);
        }
        epWrapper.orderByAsc(VideoEpisode::getSort, VideoEpisode::getEpisodeNumber);
        List<VideoEpisode> episodes = videoEpisodeMapper.selectList(epWrapper);
        List<VideoEpisodeVo> episodeVos = episodes.stream().map(ep -> {
            VideoEpisodeVo epVo = new VideoEpisodeVo();
            BeanUtils.copyProperties(ep, epVo);
            // 填充每集的播放跳转链接
            if (videoVo.getAvid() != null && epVo.getAvid() != null) {
                epVo.setPlayLink("/client/video/" + videoVo.getAvid() + "/episode/" + epVo.getAvid());
            }
            return epVo;
        }).collect(Collectors.toList());
        // 定位当前播放的剧集
        VideoEpisode currentEpisodeEntity = episodes.stream()
                .filter(ep -> episodeUid.equals(ep.getAvid()))
                .findFirst()
                .orElseThrow(() -> new GlobeException(isOwner ? "剧集不存在" : "剧集不存在或未通过审核"));
        // 剧集播放数+1（仅已发布视频统计，作者预览未发布稿件不计入播放量）
        if (published) {
            videoEpisodeMapper.update(null, new LambdaUpdateWrapper<VideoEpisode>()
                    .eq(VideoEpisode::getId, currentEpisodeEntity.getId())
                    .setSql("play_count = play_count + 1"));
            // 用户已登录时记录观看历史
            watchHistoryService.recordPlay(videoVo.getId(), currentEpisodeEntity.getId());
        }
        VideoEpisodeVo currentEpisode = new VideoEpisodeVo();
        BeanUtils.copyProperties(currentEpisodeEntity, currentEpisode);
        // 当前剧集同样填充播放跳转链接
        if (videoVo.getAvid() != null && currentEpisode.getAvid() != null) {
            currentEpisode.setPlayLink("/client/video/" + videoVo.getAvid() + "/episode/" + currentEpisode.getAvid());
        }
        // 批量统计每集评论总数（前台可见口径：排除运营屏蔽），填充到每集
        List<Long> episodeIds = episodes.stream().map(VideoEpisode::getId).collect(Collectors.toList());
        Map<Long, Long> commentCountMap = commentService.getEpisodeCommentCounts(episodeIds);
        episodeVos.forEach(epVo -> epVo.setCommentCount(commentCountMap.getOrDefault(epVo.getId(), 0L)));
        currentEpisode.setCommentCount(commentCountMap.getOrDefault(currentEpisode.getId(), 0L));
        // 组装播放详情
        ClientVideoDetailVo detail = new ClientVideoDetailVo();
        BeanUtils.copyProperties(videoVo, detail);
        detail.setEpisodes(episodeVos);
        detail.setCurrentEpisode(currentEpisode);
        // 标签：从 Video 实体获取并转换
        Video video = baseMapper.selectById(videoVo.getId());
        detail.setTags(strToTags(video.getTags()));
        // UP主个性签名（实时从 user_info 获取）
        UserInfo up = userInfoMapper.selectById(videoVo.getUserId());
        if (up != null) {
            detail.setDescript(up.getDescript());
        }
        // 当前用户是否已关注该UP主
        detail.setIsFollowed(isFollowedUp(videoVo.getUserId()));
        return detail;
    }

    /**
     * 判断当前用户是否已关注指定UP主（未登录返回 false）
     */
    private boolean isFollowedUp(Long followUserId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null || followUserId == null) {
            return false;
        }
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, currentUserId)
                .eq(Follow::getFollowUserId, followUserId);
        return followMapper.selectCount(wrapper) > 0;
    }

    @Override
    public VideoVo getDetailById(Long id) {
        if (id == null) {
            throw new GlobeException("视频ID不能为空");
        }
        // JOIN user_info 获取UP主最新信息
        VideoVo vo = baseMapper.selectDetailById(id);
        if (vo == null) {
            throw new GlobeException("视频不存在");
        }
        // 查询剧集
        LambdaQueryWrapper<VideoEpisode> epWrapper = new LambdaQueryWrapper<>();
        epWrapper.eq(VideoEpisode::getVideoId, id)
                .orderByAsc(VideoEpisode::getSort, VideoEpisode::getEpisodeNumber);
        List<VideoEpisode> episodes = videoEpisodeMapper.selectList(epWrapper);
        List<VideoEpisodeVo> episodeVos = episodes.stream().map(ep -> {
            VideoEpisodeVo epVo = new VideoEpisodeVo();
            BeanUtils.copyProperties(ep, epVo);
            return epVo;
        }).collect(Collectors.toList());
        vo.setEpisodes(episodeVos);

        // 标签：从 Video 实体获取并转换
        Video video = baseMapper.selectById(id);
        vo.setTags(strToTags(video.getTags()));

        return vo;
    }

    @Override
    public VideoVo getDetailByAvid(String avid) {
        if (avid == null || avid.isBlank()) {
            throw new GlobeException("avid 不能为空");
        }
        VideoVo vo = baseMapper.selectDetailByAvid(avid);
        if (vo == null) {
            throw new GlobeException("视频不存在");
        }
        Long videoId = vo.getId();
        // 查询剧集
        LambdaQueryWrapper<VideoEpisode> epWrapper = new LambdaQueryWrapper<>();
        epWrapper.eq(VideoEpisode::getVideoId, videoId)
                .orderByAsc(VideoEpisode::getSort, VideoEpisode::getEpisodeNumber);
        List<VideoEpisode> episodes = videoEpisodeMapper.selectList(epWrapper);
        List<VideoEpisodeVo> episodeVos = episodes.stream().map(ep -> {
            VideoEpisodeVo epVo = new VideoEpisodeVo();
            BeanUtils.copyProperties(ep, epVo);
            return epVo;
        }).collect(Collectors.toList());
        vo.setEpisodes(episodeVos);

        // 标签
        Video video = baseMapper.selectById(videoId);
        vo.setTags(strToTags(video.getTags()));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Video add(VideoSaveDto dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new GlobeException("视频标题不能为空");
        }
        // 构建视频实体
        Video video = new Video();
        BeanUtils.copyProperties(dto, video, "id", "tags");
        // 生成 Snowflake ID 并转为 avid
        long snowflakeId = snowflakeIdGenerator.nextId();
        video.setAvid(Base62Converter.toAvid(snowflakeId));
        video.setTags(tagsToStr(dto.getTags()));
        // 读取 web_config 中 video_review 配置：为 true 时投稿进入待审核，否则直接发布
        boolean autoReview = isAutoReviewEnabled();
        if (!autoReview) {
            video.setVideoStatus(2);  // 已发布
        }
        save(video);
        // 新增剧集
        if (dto.getEpisodes() != null) {
            for (int i = 0; i < dto.getEpisodes().size(); i++) {
                VideoEpisode ep = buildNewEpisode(video.getId(), dto.getEpisodes().get(i), i, autoReview);
                videoEpisodeMapper.insert(ep);
            }
        }
        // 更新视频的总集数和总时长
        recalculateVideoStats(video.getId());
        // 同步 ES 索引
        refreshVideoEs(video.getId());
        return video;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(VideoSaveDto dto) {
        if (dto.getId() == null) {
            throw new GlobeException("视频ID不能为空");
        }
        Video existVideo = baseMapper.selectById(dto.getId());
        if (existVideo == null) {
            throw new GlobeException("视频不存在");
        }
        boolean autoReview = isAutoReviewEnabled();
        // 更新视频基本信息
        BeanUtils.copyProperties(dto, existVideo, "id", "tags", "userId");
        if (!autoReview) {
            existVideo.setVideoStatus(2);  // 已发布
        }
        // 标签：List<String> → 逗号分隔字符串
        existVideo.setTags(tagsToStr(dto.getTags()));
        baseMapper.updateById(existVideo);
        // 兼容剧集维护：请求携带剧集列表时按增量逻辑同步（新增/更新/删除，保留统计与审核状态）
        if (dto.getEpisodes() != null) {
            syncEpisodes(existVideo, dto.getEpisodes());
        }
        // 同步 ES 索引
        refreshVideoEs(existVideo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new GlobeException("视频ID不能为空");
        }
        Video video = baseMapper.selectById(id);
        if (video == null) {
            throw new GlobeException("视频不存在");
        }
        // 删除剧集
        LambdaQueryWrapper<VideoEpisode> epWrapper = new LambdaQueryWrapper<>();
        epWrapper.eq(VideoEpisode::getVideoId, id);
        videoEpisodeMapper.delete(epWrapper);

        // 删除视频（标签随视频一并删除，无需单独处理）
        baseMapper.deleteById(id);
        // 删除 ES 索引文档
        deleteVideoEs(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> saveEpisodes(VideoEpisodeSaveBatchDto dto) {
        if (dto.getVideoId() == null) {
            throw new GlobeException("视频ID不能为空");
        }
        Video video = baseMapper.selectById(dto.getVideoId());
        if (video == null) {
            throw new GlobeException("视频不存在");
        }
        syncEpisodes(video, dto.getEpisodes());
        // 同步 ES 索引
        refreshVideoEs(video.getId());
        return R.success();
    }

    /**
     * 增量同步视频剧集：区分新增/更新/删除，保留统计数据和审核状态；已完结的视频不允许新增剧集
     *
     * @param video       视频实体（已完结判断依据，编辑时传入更新后的实体）
     * @param episodeDtos 剧集列表（null 时删除全部剧集）
     */
    private void syncEpisodes(Video video, List<VideoEpisodeSaveDto> episodeDtos) {
        Long videoId = video.getId();
        // 查询当前所有剧集
        LambdaQueryWrapper<VideoEpisode> epWrapper = new LambdaQueryWrapper<>();
        epWrapper.eq(VideoEpisode::getVideoId, videoId);
        List<VideoEpisode> existingEpisodes = videoEpisodeMapper.selectList(epWrapper);
        Map<Long, VideoEpisode> existingMap = existingEpisodes.stream()
                .collect(Collectors.toMap(VideoEpisode::getId, e -> e));
        // 增量更新：区分新增/更新/删除，保留统计数据和审核状态
        Set<Long> keepIds = new HashSet<>();
        if (episodeDtos != null) {
            for (int i = 0; i < episodeDtos.size(); i++) {
                VideoEpisodeSaveDto epDto = episodeDtos.get(i);
                if (epDto.getId() != null && existingMap.containsKey(epDto.getId())) {
                    // 更新已有剧集：只更新业务字段，保留统计字段；标题或播放地址变更时重置审核状态
                    VideoEpisode ep = existingMap.get(epDto.getId());
                    Integer reviewStatus = ep.getReviewStatus();
                    if (!ep.equalsObj(epDto.getTitle(), epDto.getPlayUrl())) {
                        reviewStatus = VideoConstant.REVIEW_PENDING;
                    }
                    // 显式 set 逐字段更新，允许业务字段置空（updateById 默认 NOT_NULL 策略会跳过 null 字段）
                    videoEpisodeMapper.update(null, new LambdaUpdateWrapper<VideoEpisode>()
                            .eq(VideoEpisode::getId, ep.getId())
                            .set(VideoEpisode::getEpisodeNumber, epDto.getEpisodeNumber() != null ? epDto.getEpisodeNumber() : i + 1)
                            .set(VideoEpisode::getTitle, epDto.getTitle())
                            .set(VideoEpisode::getDuration, epDto.getDuration())
                            .set(VideoEpisode::getPlayUrl, epDto.getPlayUrl())
                            .set(VideoEpisode::getSort, epDto.getSort() != null ? epDto.getSort() : i)
                            .set(VideoEpisode::getReviewStatus, reviewStatus));
                    keepIds.add(ep.getId());
                } else {
                    if (epDto.getId() != null) {
                        // 提交的剧集id在库中不存在（已被删除），按异常处理而非新增，防止已删除剧集被重新插入
                        throw new GlobeException("剧集不存在或已被删除");
                    }
                    if (!Boolean.TRUE.equals(video.getIsOngoing())) {
                        throw new GlobeException("已完结的视频不能添加新剧集");
                    }
                    // 新增剧集
                    VideoEpisode ep = buildNewEpisode(videoId, epDto, i, isAutoReviewEnabled());
                    videoEpisodeMapper.insert(ep);
                }
            }
        }
        // 删除不在保留列表中的旧剧集
        for (VideoEpisode ep : existingEpisodes) {
            if (!keepIds.contains(ep.getId())) {
                videoEpisodeMapper.deleteById(ep.getId());
            }
        }
        // 更新视频的总集数和总时长
        recalculateVideoStats(videoId);
    }

    @Override
    public void updateReviewStatus(Long episodeId, Integer reviewStatus) {
        if (episodeId == null) {
            throw new GlobeException("剧集ID不能为空");
        }
        if (reviewStatus == null || reviewStatus < VideoConstant.REVIEW_PENDING || reviewStatus > VideoConstant.REVIEW_STATUS_MAX) {
            throw new GlobeException("审核状态无效，必须为0-待审核、1-审核通过、2-审核拒绝");
        }
        VideoEpisode episode = videoEpisodeMapper.selectById(episodeId);
        if (episode == null) {
            throw new GlobeException("剧集不存在");
        }
        episode.setReviewStatus(reviewStatus);
        videoEpisodeMapper.updateById(episode);
        // 同步 ES 索引（审核结果影响剧集是否可搜索）
        refreshVideoEs(episode.getVideoId());
    }

    @Override
    public void deleteEpisode(Long episodeId) {
        if (episodeId == null) {
            throw new GlobeException("剧集ID不能为空");
        }
        VideoEpisode episode = videoEpisodeMapper.selectById(episodeId);
        if (episode == null) {
            throw new GlobeException("剧集不存在");
        }
        Long videoId = episode.getVideoId();
        videoEpisodeMapper.deleteById(episodeId);
        recalculateVideoStats(videoId);
        // 同步 ES 索引
        refreshVideoEs(videoId);
    }

    @Override
    public VideoReviewStatsVo getReviewStats() {
        VideoReviewStatsVo vo = baseMapper.selectReviewStats();
        // 计算较昨日的变化差值
        Long ydNew = vo.getYesterdayNewCount() != null ? vo.getYesterdayNewCount() : 0L;
        Long ydPending = vo.getYesterdayPendingCount() != null ? vo.getYesterdayPendingCount() : 0L;
        Long tdNew = vo.getTodayNewCount() != null ? vo.getTodayNewCount() : 0L;
        Long tdPending = vo.getTodayPendingCount() != null ? vo.getTodayPendingCount() : 0L;
        vo.setTodayNewDiff(tdNew - ydNew);
        vo.setTodayPendingDiff(tdPending - ydPending);
        // 清理内部字段，不返回给前端
        vo.setYesterdayNewCount(null);
        vo.setYesterdayPendingCount(null);
        return vo;
    }

    @Override
    public List<CategoryStatsVo> getCategoryStats() {
        // 1. 查询按 category_id 分组的视频数量
        Map<Long, Map<String, Object>> stats = baseMapper.selectCategoryStats();

        // 2. 获取分类名称映射（优先 Redis 缓存，未命中查库）
        List<CategorySimpleVo> categories = categoryService.listEnabled();
        Map<Long, String> nameMap = categories.stream()
                .collect(Collectors.toMap(CategorySimpleVo::getId, CategorySimpleVo::getName));

        // 3. 将 category_id 替换为分类中文名
        return stats.entrySet().stream().map(entry -> {
            Long categoryId = entry.getKey();
            Long videoCount = ((Number) entry.getValue().get("video_count")).longValue();
            String categoryName = nameMap.getOrDefault(categoryId, "未知分类(" + categoryId + ")");
            return new CategoryStatsVo(categoryName, videoCount);
        }).collect(Collectors.toList());
    }

    @Override
    public List<DailyIncrementVo> getDailyIncrement(LocalDate startDate, LocalDate endDate) {
        List<DailyIncrementVo> dbList = baseMapper.selectDailyIncrement(startDate, endDate);
        Map<LocalDate, DailyIncrementVo> dateMap = dbList.stream()
                .collect(Collectors.toMap(DailyIncrementVo::getDate, v -> v));
        List<DailyIncrementVo> result = new ArrayList<>();
        for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
            DailyIncrementVo vo = dateMap.get(day);
            if (vo == null) {
                vo = new DailyIncrementVo();
                vo.setDate(day);
                vo.setVideoCount(0L);
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<MonthlyContributionVo> listMyContributions(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new GlobeException("开始时间和结束时间不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new GlobeException("开始时间不能晚于结束时间");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        // 查询时间范围内的投稿（结束日期含当天，SQL 使用左闭右开区间），已按投稿时间倒序
        List<MyContributionVideoVo> videos = baseMapper.selectMyContributions(
                currentUserId, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
        // 填充播放跳转链接：优先最新审核通过剧集，无审核通过剧集时回退到最新剧集（作者预览待审核稿件）
        videos.forEach(vo -> {
            String episodeAvid = vo.getEpisodeAvid() != null ? vo.getEpisodeAvid() : vo.getLatestEpisodeAvid();
            if (vo.getAvid() != null && episodeAvid != null) {
                vo.setPlayLink("/client/video/" + vo.getAvid() + "/episode/" + episodeAvid);
            }
        });
        // 按月份分组（LinkedHashMap 保持组内投稿时间倒序）
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, List<MyContributionVideoVo>> grouped = videos.stream()
                .collect(Collectors.groupingBy(
                        vo -> vo.getCreateTime().format(monthFormatter),
                        LinkedHashMap::new,
                        Collectors.toList()));
        // 月份倒序组装
        return grouped.entrySet().stream()
                .sorted(Map.Entry.<String, List<MyContributionVideoVo>>comparingByKey().reversed())
                .map(entry -> {
                    MonthlyContributionVo group = new MonthlyContributionVo();
                    group.setMonth(entry.getKey());
                    group.setCount(entry.getValue().size());
                    group.setVideos(entry.getValue());
                    return group;
                })
                .collect(Collectors.toList());
    }

    @Override
    public IPage<MyContributionVideoVo> pageMyContributions(Integer page, Integer size, Integer videoStatus, String title) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        int pageNum = page != null ? page : 1;
        int pageSize = size != null ? size : 10;
        IPage<MyContributionVideoVo> result = baseMapper.selectMyContributionsPage(
                new Page<>(pageNum, pageSize), currentUserId, videoStatus,
                StringUtils.isEmpty(title) ? null : title.trim());
        // 填充播放跳转链接：优先最新审核通过剧集，无审核通过剧集时回退到最新剧集（作者预览待审核稿件）
        result.getRecords().forEach(vo -> {
            String episodeAvid = vo.getEpisodeAvid() != null ? vo.getEpisodeAvid() : vo.getLatestEpisodeAvid();
            if (vo.getAvid() != null && episodeAvid != null) {
                vo.setPlayLink("/client/video/" + vo.getAvid() + "/episode/" + episodeAvid);
            }
        });
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Video contribute(VideoContributeDto dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new GlobeException("视频标题不能为空");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        // 构建视频实体（身份从token解析，视频状态由后端控制）
        Video video = new Video();
        BeanUtils.copyProperties(dto, video, "videoUid", "tags", "userId", "videoStatus");
        long snowflakeId = snowflakeIdGenerator.nextId();
        video.setAvid(Base62Converter.toAvid(snowflakeId));
        video.setUserId(currentUserId);
        video.setTags(tagsToStr(dto.getTags()));
        // 审核流程跟随免审配置：免审关闭直接发布，开启则进入待发布待审核
        boolean autoReview = isAutoReviewEnabled();
        video.setVideoStatus(autoReview ? VideoConstant.VIDEO_STATUS_PENDING : VideoConstant.VIDEO_STATUS_PUBLISHED);
        save(video);
        // 新增剧集（审核状态由免审配置决定）
        if (dto.getEpisodes() != null) {
            for (int i = 0; i < dto.getEpisodes().size(); i++) {
                VideoEpisode ep = buildNewEpisode(video.getId(), dto.getEpisodes().get(i), i, autoReview);
                videoEpisodeMapper.insert(ep);
            }
        }
        // 更新视频的总集数和总时长
        recalculateVideoStats(video.getId());
        // 同步 ES 索引
        refreshVideoEs(video.getId());
        return video;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateContribution(VideoContributeDto dto) {
        if (StringUtils.isEmpty(dto.getVideoUid())) {
            throw new GlobeException("视频avid不能为空");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        Video existVideo = baseMapper.selectOne(new LambdaQueryWrapper<Video>()
                .eq(Video::getAvid, dto.getVideoUid()));
        if (existVideo == null) {
            throw new GlobeException("视频不存在");
        }
        if (!currentUserId.equals(existVideo.getUserId())) {
            throw new GlobeException("无权编辑他人的视频");
        }
        if (Objects.equals(existVideo.getVideoStatus(), VideoConstant.VIDEO_STATUS_PUBLISHED)) {
            throw new GlobeException("已发布的视频不可编辑，请联系管理员");
        }
        // 更新视频基本信息（userId、videoStatus 由后端控制）
        BeanUtils.copyProperties(dto, existVideo, "videoUid", "tags", "userId", "videoStatus");
        boolean autoReview = isAutoReviewEnabled();
        existVideo.setVideoStatus(autoReview ? VideoConstant.VIDEO_STATUS_PENDING : VideoConstant.VIDEO_STATUS_PUBLISHED);
        existVideo.setTags(tagsToStr(dto.getTags()));
        baseMapper.updateById(existVideo);
        // 兼容剧集维护：请求携带剧集列表时按增量逻辑同步（变更剧集重置审核状态）
        if (dto.getEpisodes() != null) {
            syncEpisodes(existVideo, dto.getEpisodes());
        }
        // 同步 ES 索引
        refreshVideoEs(existVideo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteContribution(String videoUid) {
        if (StringUtils.isEmpty(videoUid)) {
            throw new GlobeException("视频avid不能为空");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        Video video = baseMapper.selectOne(new LambdaQueryWrapper<Video>()
                .eq(Video::getAvid, videoUid));
        if (video == null) {
            throw new GlobeException("视频不存在");
        }
        if (!currentUserId.equals(video.getUserId())) {
            throw new GlobeException("无权删除他人的视频");
        }
        // 删除剧集
        LambdaQueryWrapper<VideoEpisode> epWrapper = new LambdaQueryWrapper<>();
        epWrapper.eq(VideoEpisode::getVideoId, video.getId());
        videoEpisodeMapper.delete(epWrapper);
        // 删除视频（标签随视频一并删除，无需单独处理）
        baseMapper.deleteById(video.getId());
        // 删除 ES 索引文档
        deleteVideoEs(video.getId());
    }

    @Override
    public VideoVo getContributionDetail(String videoUid) {
        if (StringUtils.isEmpty(videoUid)) {
            throw new GlobeException("视频avid不能为空");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        VideoVo vo = getDetailByAvid(videoUid);
        if (!currentUserId.equals(vo.getUserId())) {
            throw new GlobeException("无权查看他人的投稿");
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean toggleOngoing(String videoUid) {
        if (StringUtils.isEmpty(videoUid)) {
            throw new GlobeException("视频avid不能为空");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        Video video = baseMapper.selectOne(new LambdaQueryWrapper<Video>()
                .eq(Video::getAvid, videoUid));
        if (video == null) {
            throw new GlobeException("视频不存在");
        }
        // 校验上传者身份：仅视频上传者可切换
        if (!currentUserId.equals(video.getUserId())) {
            throw new GlobeException("无权修改他人的视频");
        }
        // toggle：连载中→已完结，已完结→连载中
        boolean newOngoing = !Boolean.TRUE.equals(video.getIsOngoing());
        baseMapper.update(null, new LambdaUpdateWrapper<Video>()
                .eq(Video::getId, video.getId())
                .set(Video::getIsOngoing, newOngoing));
        // 同步 ES 索引
        refreshVideoEs(video.getId());
        return newOngoing;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VideoEpisode addEpisode(VideoEpisodeAddDto dto) {
        if (StringUtils.isEmpty(dto.getVideoUid())) {
            throw new GlobeException("视频avid不能为空");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        Video video = baseMapper.selectOne(new LambdaQueryWrapper<Video>()
                .eq(Video::getAvid, dto.getVideoUid()));
        if (video == null) {
            throw new GlobeException("视频不存在");
        }
        // 校验上传者身份：仅视频上传者可添加剧集
        if (!currentUserId.equals(video.getUserId())) {
            throw new GlobeException("无权修改他人的视频");
        }
        // 仅连载中的视频可添加新剧集
        if (!Boolean.TRUE.equals(video.getIsOngoing())) {
            throw new GlobeException("已完结的视频不能添加新剧集");
        }
        // 集数序号与排序不传时自动追加到末尾（最大集数序号+1、最大排序+1）
        List<VideoEpisode> episodes = videoEpisodeMapper.selectList(new LambdaQueryWrapper<VideoEpisode>()
                .eq(VideoEpisode::getVideoId, video.getId()));
        int maxEpisodeNumber = episodes.stream()
                .mapToInt(e -> e.getEpisodeNumber() != null ? e.getEpisodeNumber() : 0)
                .max().orElse(0);
        int maxSort = episodes.stream()
                .mapToInt(e -> e.getSort() != null ? e.getSort() : -1)
                .max().orElse(-1);
        if (dto.getEpisodeNumber() == null) {
            dto.setEpisodeNumber(maxEpisodeNumber + 1);
        }
        if (dto.getSort() == null) {
            dto.setSort(maxSort + 1);
        }
        // 新增剧集（审核状态由免审配置决定，统计字段初始化为0）
        VideoEpisode ep = buildNewEpisode(video.getId(), dto, 0, isAutoReviewEnabled());
        videoEpisodeMapper.insert(ep);
        // 更新视频的总集数和总时长
        recalculateVideoStats(video.getId());
        // 同步 ES 索引
        refreshVideoEs(video.getId());
        return ep;
    }

    @Override
    public IPage<VideoEpisodeVo> pageMyEpisodes(EpisodePageDto dto) {
        if (StringUtils.isEmpty(dto.getVideoUid())) {
            throw new GlobeException("视频avid不能为空");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        Video video = baseMapper.selectOne(new LambdaQueryWrapper<Video>()
                .eq(Video::getAvid, dto.getVideoUid()));
        if (video == null) {
            throw new GlobeException("视频不存在");
        }
        // 校验上传者身份：仅视频上传者可查询剧集
        if (!currentUserId.equals(video.getUserId())) {
            throw new GlobeException("无权查看他人的投稿");
        }
        int pageNum = dto.getPage() != null ? dto.getPage() : 1;
        int pageSize = dto.getSize() != null ? dto.getSize() : 10;
        // 多条件：标题模糊搜索 + 审核状态筛选，按排序与集数序号升序
        LambdaQueryWrapper<VideoEpisode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VideoEpisode::getVideoId, video.getId());
        if (StringUtils.isNotEmpty(dto.getTitle())) {
            wrapper.like(VideoEpisode::getTitle, dto.getTitle().trim());
        }
        if (dto.getReviewStatus() != null) {
            wrapper.eq(VideoEpisode::getReviewStatus, dto.getReviewStatus());
        }
        wrapper.orderByDesc(VideoEpisode::getSort, VideoEpisode::getEpisodeNumber);
        IPage<VideoEpisode> pageResult = videoEpisodeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        // 转换为VO并填充播放跳转链接
        IPage<VideoEpisodeVo> result = new Page<>(pageNum, pageSize, pageResult.getTotal());
        result.setRecords(pageResult.getRecords().stream().map(ep -> {
            VideoEpisodeVo vo = new VideoEpisodeVo();
            BeanUtils.copyProperties(ep, vo);
            vo.setPlayLink("/client/video/" + video.getAvid() + "/episode/" + ep.getAvid());
            return vo;
        }).collect(Collectors.toList()));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEpisode(EpisodeUpdateDto dto) {
        if (StringUtils.isEmpty(dto.getEpisodeUid())) {
            throw new GlobeException("剧集avid不能为空");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        VideoEpisode episode = videoEpisodeMapper.selectOne(new LambdaQueryWrapper<VideoEpisode>()
                .eq(VideoEpisode::getAvid, dto.getEpisodeUid()));
        if (episode == null) {
            throw new GlobeException("剧集不存在");
        }
        Video video = baseMapper.selectById(episode.getVideoId());
        if (video == null) {
            throw new GlobeException("视频不存在");
        }
        // 校验上传者身份：仅视频上传者可编辑剧集
        if (!currentUserId.equals(video.getUserId())) {
            throw new GlobeException("无权修改他人的视频");
        }
        // 标题或播放地址变更时重置审核状态为待审核（与投稿编辑同步逻辑一致）
        Integer reviewStatus = episode.getReviewStatus();
        if (!episode.equalsObj(dto.getTitle(), dto.getPlayUrl())) {
            reviewStatus = VideoConstant.REVIEW_PENDING;
        }
        // 显式 set 逐字段更新，允许标题与播放地址置空；集数序号与排序不传时保持原值
        videoEpisodeMapper.update(null, new LambdaUpdateWrapper<VideoEpisode>()
                .eq(VideoEpisode::getId, episode.getId())
                .set(dto.getEpisodeNumber() != null, VideoEpisode::getEpisodeNumber, dto.getEpisodeNumber())
                .set(VideoEpisode::getTitle, dto.getTitle())
                .set(VideoEpisode::getDuration, dto.getDuration())
                .set(VideoEpisode::getPlayUrl, dto.getPlayUrl())
                .set(dto.getSort() != null, VideoEpisode::getSort, dto.getSort())
                .set(VideoEpisode::getReviewStatus, reviewStatus));
        // 时长可能变化，重算视频的总集数和总时长
        recalculateVideoStats(video.getId());
        // 同步 ES 索引（标题/时长/审核状态变更影响搜索展示）
        refreshVideoEs(video.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEpisodeByUid(String episodeUid) {
        if (StringUtils.isEmpty(episodeUid)) {
            throw new GlobeException("剧集avid不能为空");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        VideoEpisode episode = videoEpisodeMapper.selectOne(new LambdaQueryWrapper<VideoEpisode>()
                .eq(VideoEpisode::getAvid, episodeUid));
        if (episode == null) {
            throw new GlobeException("剧集不存在");
        }
        Video video = baseMapper.selectById(episode.getVideoId());
        if (video == null) {
            throw new GlobeException("视频不存在");
        }
        // 校验上传者身份：仅视频上传者可删除剧集
        if (!currentUserId.equals(video.getUserId())) {
            throw new GlobeException("无权修改他人的视频");
        }
        // 仅连载中的视频可删除剧集（与添加剧集对称）
        if (!Boolean.TRUE.equals(video.getIsOngoing())) {
            throw new GlobeException("已完结的视频不能删除剧集");
        }
        videoEpisodeMapper.deleteById(episode.getId());
        // 重算视频的总集数和总时长
        recalculateVideoStats(video.getId());
        // 同步 ES 索引
        refreshVideoEs(video.getId());
    }

    @Override
    public DataCenterVo getDataCenter() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        // 总量统计：自己所有投稿剧集的观看/收藏/投币/点赞累计（无投稿时置0）
        DataCenterVo vo = baseMapper.selectDataCenterStats(currentUserId);
        if (vo == null) {
            vo = new DataCenterVo();
            vo.setTotalPlayCount(0L);
            vo.setTotalFavoriteCount(0L);
            vo.setTotalCoinCount(0L);
            vo.setTotalLikeCount(0L);
        }
        // 榜单：最热视频TOP5（观看数最多）与最受欢迎视频TOP5（收藏数最多），填充播放跳转链接
        List<ClientVideoVo> hotVideos = baseMapper.selectHotVideos(currentUserId, BaseConstant.DATA_CENTER_TOP_SIZE);
        List<ClientVideoVo> favoriteVideos = baseMapper.selectFavoriteVideos(currentUserId, BaseConstant.DATA_CENTER_TOP_SIZE);
        fillPlayLink(hotVideos);
        fillPlayLink(favoriteVideos);
        vo.setHotVideos(hotVideos);
        vo.setFavoriteVideos(favoriteVideos);
        return vo;
    }

    @Override
    public IPage<ClientVideoVo> pageSearchVideo(String keyword, Integer page, Integer size, String sortType) {
        // 搜索实现（ES/数据库适配器）由 SearchServiceFacade 按开关路由，ES 异常自动降级数据库
        IPage<ClientVideoVo> result = searchServiceFacade.searchVideo(keyword, page, size, sortType);
        // 填充播放跳转链接与关注标记
        fillPlayLink(result.getRecords());
        fillFollowStatus(result.getRecords());
        return result;
    }

    /**
     * 视频变更后同步 ES 索引（ES 未装配或同步失败时静默忽略，不影响主流程）
     */
    private void refreshVideoEs(Long videoId) {
        EsSearchService esSearchService = esSearchServiceProvider.getIfAvailable();
        if (esSearchService == null) {
            return;
        }
        try {
            esSearchService.refreshVideo(videoId);
        } catch (Exception e) {
            log.warn("同步视频到 ES 失败 videoId={}: {}", videoId, e.getMessage());
        }
    }

    /**
     * 删除视频 ES 索引文档（ES 未装配或删除失败时静默忽略，不影响主流程）
     */
    private void deleteVideoEs(Long videoId) {
        EsSearchService esSearchService = esSearchServiceProvider.getIfAvailable();
        if (esSearchService == null) {
            return;
        }
        try {
            esSearchService.deleteVideo(videoId);
        } catch (Exception e) {
            log.warn("删除 ES 视频文档失败 videoId={}: {}", videoId, e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 构建新剧集实体（审核状态由免审配置决定，统计字段初始化为0）
     *
     * @param videoId   视频ID
     * @param epDto     剧集DTO
     * @param index     在列表中的索引（用于自动编号和排序）
     * @param autoReview 免审是否开启
     */
    private VideoEpisode buildNewEpisode(Long videoId, VideoEpisodeSaveDto epDto, int index, boolean autoReview) {
        VideoEpisode ep = new VideoEpisode();
        ep.setVideoId(videoId);
        // 生成 Snowflake ID 并转为剧集 avid
        long snowflakeId = snowflakeIdGenerator.nextId();
        ep.setAvid(Base62Converter.toAvid(snowflakeId));
        ep.setReviewStatus(!autoReview ? VideoConstant.REVIEW_APPROVED : VideoConstant.REVIEW_PENDING);
        ep.setEpisodeNumber(epDto.getEpisodeNumber() != null ? epDto.getEpisodeNumber() : index + 1);
        ep.setTitle(epDto.getTitle());
        ep.setDuration(epDto.getDuration());
        ep.setPlayUrl(epDto.getPlayUrl());
        ep.setSort(epDto.getSort() != null ? epDto.getSort() : index);
        ep.setPlayCount(0L);
        ep.setFavoriteCount(0L);
        ep.setLikeCount(0L);
        ep.setCoinCount(0L);
        ep.setViewerCount(0L);
        return ep;
    }

    /**
     * 根据已有剧集重新计算视频的总集数和总时长
     */
    private void recalculateVideoStats(Long videoId) {
        LambdaQueryWrapper<VideoEpisode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VideoEpisode::getVideoId, videoId);
        List<VideoEpisode> episodes = videoEpisodeMapper.selectList(wrapper);
        Video video = baseMapper.selectById(videoId);
        if (video != null) {
            video.setEpisodeCount(episodes.size());
            long totalDuration = episodes.stream()
                    .mapToLong(e -> e.getDuration() != null ? e.getDuration() : 0L)
                    .sum();
            video.setTotalDuration(totalDuration);
            baseMapper.updateById(video);
        }
    }

    /**
     * 将 Video 实体转换为 VideoVo，同时查询剧集和标签
     */
    private VideoVo toVideoVo(Video video) {
        VideoVo vo = new VideoVo();
        BeanUtils.copyProperties(video, vo);

        Long videoId = video.getId();

        // 查询剧集
        LambdaQueryWrapper<VideoEpisode> epWrapper = new LambdaQueryWrapper<>();
        epWrapper.eq(VideoEpisode::getVideoId, videoId)
                .orderByAsc(VideoEpisode::getSort, VideoEpisode::getEpisodeNumber);
        List<VideoEpisode> episodes = videoEpisodeMapper.selectList(epWrapper);
        List<VideoEpisodeVo> episodeVos = episodes.stream().map(ep -> {
            VideoEpisodeVo epVo = new VideoEpisodeVo();
            BeanUtils.copyProperties(ep, epVo);
            return epVo;
        }).collect(Collectors.toList());
        vo.setEpisodes(episodeVos);

        // 计算视频整体审核状态
        if (episodes.isEmpty()) {
            vo.setReviewStatus(VideoConstant.REVIEW_PENDING);
        } else {
            boolean allUnreviewed = episodes.stream()
                    .allMatch(e -> e.getReviewStatus() == null || e.getReviewStatus() == VideoConstant.REVIEW_PENDING);
            boolean anyUnreviewed = episodes.stream()
                    .anyMatch(e -> e.getReviewStatus() == null || e.getReviewStatus() == VideoConstant.REVIEW_PENDING);
            if (allUnreviewed) {
                vo.setReviewStatus(VideoConstant.REVIEW_PENDING);
            } else if (anyUnreviewed) {
                vo.setReviewStatus(VideoConstant.REVIEW_APPROVED);
            } else {
                vo.setReviewStatus(VideoConstant.REVIEW_REJECTED);
            }
        }

        // 标签：逗号分隔字符串 → List<String>
        vo.setTags(strToTags(video.getTags()));

        return vo;
    }

    /**
     * 标签列表 → 逗号分隔字符串
     */
    private String tagsToStr(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return String.join(",", tags);
    }

    /**
     * 逗号分隔字符串 → 标签列表
     */
    private List<String> strToTags(String tagsStr) {
        if (tagsStr == null || tagsStr.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tagsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 读取 web_config 中 video_review 配置（统一走 WebConfigService：Redis 缓存优先，未命中回源数据库）
     * @return true 表示开启审核（投稿/新增剧集进入待审核状态），false 表示直接发布
     */
    private boolean isAutoReviewEnabled() {
        return webConfigService.getConfigBoolean(
                BaseConstant.WEB_CONFIG_CATEGORY_REVIEW, BaseConstant.WEB_CONFIG_KEY_VIDEO_REVIEW);
    }

    /**
     * 将前端传参的排序字段编码映射为 SELECT 别名，不在白名单内则返回 null（走默认排序）
     */
    private String resolveColumn(Integer field) {
        if (field == null) {
            return null;
        }
        return FIELD_COLUMN_MAP.get(field);
    }

    /**
     * 校验排序方向，仅允许 asc/desc（不区分大小写），非法值返回 null（走默认排序）
     */
    private String resolveDirection(String descOrAsc) {
        if (descOrAsc == null || descOrAsc.isBlank()) {
            return null;
        }
        String upper = descOrAsc.toUpperCase();
        return ALLOWED_DIRECTIONS.contains(upper) ? upper : null;
    }

    /**
     * 从请求 JWT token 中获取当前用户ID
     */
    private Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader("authorization");
            if (StringUtils.isEmpty(token)) {
                return null;
            }
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            return JWTUtil.getUserId(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 为前台视频记录填充当前用户是否已关注UP主的标记（未登录时全部为 false）
     */
    private void fillFollowStatus(List<ClientVideoVo> vos) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            vos.forEach(vo -> vo.setIsFollowed(false));
            return;
        }
        // 一次查询当前用户的全部关注记录，避免逐条查询
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, currentUserId);
        Set<Long> followedIds = followMapper.selectList(wrapper).stream()
                .map(Follow::getFollowUserId)
                .collect(Collectors.toSet());
        vos.forEach(vo -> vo.setIsFollowed(followedIds.contains(vo.getUserId())));
    }

    @Override
    public VideoEpisodeVo getEpisodeByAvid(String avid) {
        if (avid == null || avid.isBlank()) {
            throw new GlobeException("avid 不能为空");
        }
        LambdaQueryWrapper<VideoEpisode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VideoEpisode::getAvid, avid);
        VideoEpisode episode = videoEpisodeMapper.selectOne(wrapper);
        if (episode == null) {
            throw new GlobeException("未找到对应剧集");
        }
        // 查询父视频信息
        Video video = baseMapper.selectById(episode.getVideoId());
        VideoEpisodeVo vo = new VideoEpisodeVo();
        BeanUtils.copyProperties(episode, vo);
        // 额外设置视频相关信息
        if (video != null) {
            vo.setVideoTitle(video.getTitle());
            vo.setVideoCover(video.getCoverUrl());
            vo.setVideoAvid(video.getAvid());
            vo.setEpisodeCount(video.getEpisodeCount());
        }
        return vo;
    }

    @Override
    public IPage<VideoPageDto> pageReview(Integer page, Integer size, String title, String nickName) {
        Page<VideoPageDto> pageParam = new Page<>(page, size);
        return baseMapper.selectReviewPage(pageParam, title, nickName);
    }
}
