package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.VideoEpisodeMapper;
import com.cjym.videoweb.mapper.VideoMapper;
import com.cjym.videoweb.mapper.WebConfigMapper;
import com.cjym.videoweb.constant.VideoConstant;
import com.cjym.videoweb.model.dto.VideoEpisodeSaveBatchDto;
import com.cjym.videoweb.model.dto.VideoEpisodeSaveDto;
import com.cjym.videoweb.model.dto.VideoPageDto;
import com.cjym.videoweb.model.dto.VideoSaveDto;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.entity.VideoEpisode;
import com.cjym.videoweb.model.entity.WebConfig;
import com.cjym.videoweb.model.vo.VideoEpisodeVo;
import com.cjym.videoweb.model.vo.VideoReviewStatsVo;
import com.cjym.videoweb.model.vo.VideoVo;
import com.cjym.videoweb.model.vo.WebConfigVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.VideoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    @Resource
    private VideoEpisodeMapper videoEpisodeMapper;

    @Resource
    private WebConfigMapper webConfigMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

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
                                          Long categoryId, Integer reviewStatus, String nickName, String uid,
                                          Integer field, String descOrAsc,Boolean isOngoing) {
        Page<VideoPageDto> pageParam = new Page<>(page, size);
        String orderColumn = resolveColumn(field);
        String orderDirection = resolveDirection(descOrAsc);
        return baseMapper.selectVideoPage(pageParam, title, categoryId, reviewStatus, nickName, uid,
                orderColumn, orderDirection,isOngoing);
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
    @Transactional(rollbackFor = Exception.class)
    public void add(VideoSaveDto dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new GlobeException("视频标题不能为空");
        }
        // 构建视频实体
        Video video = new Video();
        BeanUtils.copyProperties(dto, video, "id", "tags");
        // 标签：List<String> → 逗号分隔字符串
        video.setTags(tagsToStr(dto.getTags()));

        // 读取 web_config 中 video_review 配置，若为 false 则自动发布并审核通过
        boolean autoReview = isAutoReviewEnabled();
        if (!autoReview) {
            video.setVideoStatus(2);  // 已发布
        }
        baseMapper.insert(video);
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
        Long videoId = dto.getVideoId();
        List<VideoEpisodeSaveDto> episodeDtos = dto.getEpisodes();
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
                    // 更新已有剧集：只更新业务字段，保留统计字段和审核状态
                    VideoEpisode ep = existingMap.get(epDto.getId());
                    if (ep.equalsObj(epDto.getTitle(), epDto.getPlayUrl())) {
                        ep.setReviewStatus(ep.getReviewStatus());
                    } else {
                        ep.setReviewStatus(VideoConstant.REVIEW_PENDING);
                    }
                    ep.setEpisodeNumber(epDto.getEpisodeNumber() != null ? epDto.getEpisodeNumber() : i + 1);
                    ep.setTitle(epDto.getTitle());
                    ep.setDuration(epDto.getDuration());
                    ep.setPlayUrl(epDto.getPlayUrl());
                    ep.setSort(epDto.getSort() != null ? epDto.getSort() : i);
                    videoEpisodeMapper.updateById(ep);
                    keepIds.add(ep.getId());
                } else {
                    if(!video.getIsOngoing())
                        return R.error("已完结的视频不能添加新剧集");
                    // 新增剧集
                    VideoEpisode ep = new VideoEpisode();
                    ep.setVideoId(videoId);
                    if(isAutoReviewEnabled())
                        ep.setReviewStatus(VideoConstant.REVIEW_APPROVED);
                    else
                        ep.setReviewStatus(VideoConstant.REVIEW_PENDING);
                    ep.setEpisodeNumber(epDto.getEpisodeNumber() != null ? epDto.getEpisodeNumber() : i + 1);
                    ep.setTitle(epDto.getTitle());
                    ep.setDuration(epDto.getDuration());
                    ep.setPlayUrl(epDto.getPlayUrl());
                    ep.setSort(epDto.getSort() != null ? epDto.getSort() : i);
                    ep.setPlayCount(0L);
                    ep.setFavoriteCount(0L);
                    ep.setLikeCount(0L);
                    ep.setCoinCount(0L);
                    ep.setViewerCount(0L);
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
        return R.success();
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
    }

    @Override
    public VideoReviewStatsVo getReviewStats() {
        return baseMapper.selectReviewStats();
    }

    // ==================== 私有方法 ====================

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
     * 读取 web_config 中 video_review 配置（优先 Redis 缓存，未命中查库）
     * @return true 表示开启免审自动发布
     */
    private boolean isAutoReviewEnabled() {
        String redisKey = "web_config:list:review";
        // 1. 尝试从 Redis 缓存读取
        String cached = stringRedisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isNotEmpty(cached)) {
            try {
                List<WebConfigVo> list = objectMapper.readValue(cached,
                        new TypeReference<List<WebConfigVo>>() {});
                for (WebConfigVo vo : list) {
                    if ("video_review".equals(vo.getKey())) {
                        return "true".equalsIgnoreCase(vo.getValue());
                    }
                }
                return false;
            } catch (JsonProcessingException ignored) {
                // 缓存数据异常，回源数据库
            }
        }
        // 2. Redis 未命中或数据异常，查数据库
        LambdaQueryWrapper<WebConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WebConfig::getCategory, "review")
                .eq(WebConfig::getKey, "video_review");
        WebConfig config = webConfigMapper.selectOne(wrapper);
        return config != null && "true".equalsIgnoreCase(config.getValue());
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
}
