package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.dto.VideoPageDto;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.vo.VideoReviewStatsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {

    /**
     * 分页查询视频列表（关联分类表获取分类名称）
     */
    IPage<VideoPageDto> selectVideoPage(Page<VideoPageDto> page,
                                         @Param("title") String title,
                                         @Param("categoryId") Long categoryId,
                                         @Param("reviewStatus") Integer reviewStatus,
                                         @Param("nickName") String nickName,
                                         @Param("uid") String uid,
                                         @Param("orderColumn") String orderColumn,
                                         @Param("orderDirection") String orderDirection,
                                         @Param("isOngoing") Boolean isOngoing);

    /**
     * 查询视频详情（JOIN user_info 获取UP主最新信息）
     */
    com.cjym.videoweb.model.vo.VideoVo selectDetailById(@Param("id") Long id);

    /**
     * 查询视频审核状态统计（按视频级聚合审核状态分组计数）
     */
    VideoReviewStatsVo selectReviewStats();

    /**
     * 按分类统计视频数量（排除 category_id 为空的视频）
     */
    List<Map<String, Object>> selectCategoryStats();
}
