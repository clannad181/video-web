package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.VideoEpisodeSaveBatchDto;
import com.cjym.videoweb.model.dto.VideoPageDto;
import com.cjym.videoweb.model.dto.VideoSaveDto;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.vo.CategoryStatsVo;
import com.cjym.videoweb.model.vo.VideoEpisodeVo;
import com.cjym.videoweb.model.vo.VideoReviewStatsVo;
import com.cjym.videoweb.model.vo.VideoVo;
import com.cjym.videoweb.result.R;

import java.util.List;

public interface VideoService extends IService<Video> {

    /**
     * 分页查询视频列表
     */
    IPage<VideoPageDto> pageVideo(Integer page, Integer size, String title,
                                   Long categoryId, Integer videoStates, String nickName, String uid,
                                   Integer field, String descOrAsc,Boolean isOngoing,String avid);

    /**
     * 查询视频详情（含剧集和标签）
     */
    VideoVo getDetailById(Long id);

    /**
     * 根据 avid 查询视频详情（含剧集和标签）
     */
    VideoVo getDetailByAvid(String avid);

    /**
     * 新增视频
     */
    Video add(VideoSaveDto dto);

    /**
     * 编辑视频
     */
    void update(VideoSaveDto dto);

    /**
     * 删除视频（级联删除剧集和标签）
     */
    void delete(Long id);

    /**
     * 批量保存剧集（增量更新：区分新增/更新/删除）
     *
     * @return
     */
    R<String> saveEpisodes(VideoEpisodeSaveBatchDto dto);

    /**
     * 审核剧集
     */
    void updateReviewStatus(Long episodeId, Integer reviewStatus);
    /**
     * 删除剧集
     */
    void deleteEpisode(Long episodeId);

    /**
     * 查询视频审核状态统计（按视频级聚合审核状态分组计数）
     */
    VideoReviewStatsVo getReviewStats();

    /**
     * 查询视频分类统计（按分类分组计数，categoryId 替换为分类中文名）
     */
    List<CategoryStatsVo> getCategoryStats();

    /**
     * 根据剧集 avid 跳转指定集数（返回剧集+视频基本信息）
     */
    VideoEpisodeVo getEpisodeByAvid(String avid);
}
