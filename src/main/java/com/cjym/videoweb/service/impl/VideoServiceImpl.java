package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.VideoEpisodeMapper;
import com.cjym.videoweb.mapper.VideoMapper;
import com.cjym.videoweb.model.dto.VideoEpisodeSaveDto;
import com.cjym.videoweb.model.dto.VideoPageDto;
import com.cjym.videoweb.model.dto.VideoSaveDto;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.entity.VideoEpisode;
import com.cjym.videoweb.model.vo.VideoEpisodeVo;
import com.cjym.videoweb.model.vo.VideoVo;
import com.cjym.videoweb.service.VideoService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    @Resource
    private VideoEpisodeMapper videoEpisodeMapper;

    @Override
    public IPage<VideoPageDto> pageVideo(Integer page, Integer size, String title,
                                          Long categoryId, Integer reviewStatus, Long userId) {
        Page<VideoPageDto> pageParam = new Page<>(page, size);
        return baseMapper.selectVideoPage(pageParam, title, categoryId, reviewStatus, userId);
    }

    @Override
    public VideoVo getDetailById(Long id) {
        if (id == null) {
            throw new GlobeException("视频ID不能为空");
        }
        Video video = baseMapper.selectById(id);
        if (video == null) {
            throw new GlobeException("视频不存在");
        }
        return toVideoVo(video);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(VideoSaveDto dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new GlobeException("视频标题不能为空");
        }
        // 构建视频实体
        Video video = new Video();
        BeanUtils.copyProperties(dto, video, "id", "tags", "episodes");
        // 标签：List<String> → 逗号分隔字符串
        video.setTags(tagsToStr(dto.getTags()));

        // 计算总时长和总集数
        List<VideoEpisodeSaveDto> episodeDtos = dto.getEpisodes();
        if (episodeDtos != null && !episodeDtos.isEmpty()) {
            video.setEpisodeCount(episodeDtos.size());
            long totalDuration = episodeDtos.stream()
                    .mapToLong(e -> e.getDuration() != null ? e.getDuration() : 0L)
                    .sum();
            video.setTotalDuration(totalDuration);
        }
        // 初始化计数
        video.setPlayCount(0L);
        video.setFavoriteCount(0L);
        video.setLikeCount(0L);
        video.setCoinCount(0L);
        video.setViewerCount(0L);
        // 默认审核状态：待审核
        video.setReviewStatus(0);
        baseMapper.insert(video);

        Long videoId = video.getId();

        // 保存剧集
        if (episodeDtos != null && !episodeDtos.isEmpty()) {
            List<VideoEpisode> episodes = new ArrayList<>();
            for (int i = 0; i < episodeDtos.size(); i++) {
                VideoEpisodeSaveDto epDto = episodeDtos.get(i);
                VideoEpisode ep = new VideoEpisode();
                ep.setVideoId(videoId);
                ep.setEpisodeNumber(epDto.getEpisodeNumber() != null ? epDto.getEpisodeNumber() : i + 1);
                ep.setTitle(epDto.getTitle());
                ep.setDuration(epDto.getDuration());
                ep.setPlayUrl(epDto.getPlayUrl());
                ep.setSort(epDto.getSort() != null ? epDto.getSort() : i);
                episodes.add(ep);
            }
            for (VideoEpisode ep : episodes) {
                videoEpisodeMapper.insert(ep);
            }
        }
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

        // 更新视频基本信息
        BeanUtils.copyProperties(dto, existVideo, "id", "tags", "episodes",
                "playCount", "favoriteCount", "likeCount", "coinCount", "viewerCount",
                "reviewStatus", "userId", "userName", "nickName");
        // 标签：List<String> → 逗号分隔字符串
        existVideo.setTags(tagsToStr(dto.getTags()));

        // 重新计算总时长和总集数
        List<VideoEpisodeSaveDto> episodeDtos = dto.getEpisodes();
        if (episodeDtos != null && !episodeDtos.isEmpty()) {
            existVideo.setEpisodeCount(episodeDtos.size());
            long totalDuration = episodeDtos.stream()
                    .mapToLong(e -> e.getDuration() != null ? e.getDuration() : 0L)
                    .sum();
            existVideo.setTotalDuration(totalDuration);
        }
        baseMapper.updateById(existVideo);

        Long videoId = dto.getId();

        // 更新剧集：先删后增
        LambdaQueryWrapper<VideoEpisode> epWrapper = new LambdaQueryWrapper<>();
        epWrapper.eq(VideoEpisode::getVideoId, videoId);
        videoEpisodeMapper.delete(epWrapper);

        if (episodeDtos != null && !episodeDtos.isEmpty()) {
            List<VideoEpisode> episodes = new ArrayList<>();
            for (int i = 0; i < episodeDtos.size(); i++) {
                VideoEpisodeSaveDto epDto = episodeDtos.get(i);
                VideoEpisode ep = new VideoEpisode();
                ep.setVideoId(videoId);
                ep.setEpisodeNumber(epDto.getEpisodeNumber() != null ? epDto.getEpisodeNumber() : i + 1);
                ep.setTitle(epDto.getTitle());
                ep.setDuration(epDto.getDuration());
                ep.setPlayUrl(epDto.getPlayUrl());
                ep.setSort(epDto.getSort() != null ? epDto.getSort() : i);
                episodes.add(ep);
            }
            for (VideoEpisode ep : episodes) {
                videoEpisodeMapper.insert(ep);
            }
        }
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
    public void updateReviewStatus(Long id, Integer reviewStatus) {
        if (id == null) {
            throw new GlobeException("视频ID不能为空");
        }
        if (reviewStatus == null || reviewStatus < 0 || reviewStatus > 2) {
            throw new GlobeException("审核状态无效，必须为0-待审核、1-审核通过、2-审核拒绝");
        }
        Video video = baseMapper.selectById(id);
        if (video == null) {
            throw new GlobeException("视频不存在");
        }
        video.setReviewStatus(reviewStatus);
        baseMapper.updateById(video);
    }

    // ==================== 私有方法 ====================

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
}
