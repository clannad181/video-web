package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.VideoPageDto;
import com.cjym.videoweb.model.dto.VideoSaveDto;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.vo.VideoVo;

public interface VideoService extends IService<Video> {

    /**
     * 分页查询视频列表
     */
    IPage<VideoPageDto> pageVideo(Integer page, Integer size, String title,
                                   Long categoryId, Integer reviewStatus, Long userId);

    /**
     * 查询视频详情（含剧集和标签）
     */
    VideoVo getDetailById(Long id);

    /**
     * 新增视频（含剧集和标签）
     */
    void add(VideoSaveDto dto);

    /**
     * 编辑视频（含剧集和标签）
     */
    void update(VideoSaveDto dto);

    /**
     * 删除视频（级联删除剧集和标签）
     */
    void delete(Long id);

    /**
     * 更新审核状态
     */
    void updateReviewStatus(Long id, Integer reviewStatus);
}
