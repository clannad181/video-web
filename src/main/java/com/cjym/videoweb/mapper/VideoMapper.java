package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.dto.VideoPageDto;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.vo.CategoryDailyIncrementVo;
import com.cjym.videoweb.model.vo.ClientVideoVo;
import com.cjym.videoweb.model.vo.DailyIncrementVo;
import com.cjym.videoweb.model.vo.VideoReviewStatsVo;
import com.cjym.videoweb.model.vo.VideoVo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
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
                                         @Param("videoStates") Integer videoStates,
                                         @Param("nickName") String nickName,
                                         @Param("uid") String uid,
                                         @Param("orderColumn") String orderColumn,
                                         @Param("orderDirection") String orderDirection,
                                         @Param("isOngoing") Boolean isOngoing,
                                         @Param("avid") String avid);

    /**
     * 分页查询前台展示视频（仅已发布且至少有一集审核通过），支持最新/最火热排序
     */
    IPage<ClientVideoVo> selectClientVideoPage(Page<ClientVideoVo> page,
                                               @Param("orderColumn") String orderColumn);

    /**
     * 相关视频推荐分页：同UP主或标签有交集的视频，排除自身，仅已发布且至少有一集审核通过，
     * 同UP主的视频优先，再按播放量降序
     */
    IPage<ClientVideoVo> selectRelatedVideoPage(Page<ClientVideoVo> page,
                                                @Param("videoId") Long videoId,
                                                @Param("userId") Long userId,
                                                @Param("tags") String tags);

    /**
     * 查询关注UP主的最新视频（已发布且至少有一集审核通过），按发布时间倒序
     */
    List<ClientVideoVo> selectFollowLatest(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 查询最多收藏的视频TOP N（已发布且至少有一集审核通过），可排除指定视频
     */
    List<ClientVideoVo> selectFavoriteTop(@Param("limit") int limit,
                                          @Param("excludeIds") List<Long> excludeIds);

    /**
     * 查询视频详情（JOIN user_info 获取UP主最新信息）
     */
    VideoVo selectDetailById(@Param("id") Long id);

    /**
     * 根据 avid 查询视频详情
     */
    VideoVo selectDetailByAvid(@Param("avid") String avid);

    /**
     * 查询视频审核状态统计（按视频级聚合审核状态分组计数）
     */
    VideoReviewStatsVo selectReviewStats();

    /**
     * 按分类统计视频数量（排除 category_id 为空的视频）
     */
    @MapKey("category_id")
    Map<Long, Map<String, Object>> selectCategoryStats();

    /**
     * 分页查询待审核视频（video_status = 1 且 review_status = 0 或 1），支持按标题和UP主昵称模糊搜索
     */
    IPage<VideoPageDto> selectReviewPage(Page<VideoPageDto> page,
                                          @Param("title") String title,
                                          @Param("nickName") String nickName);

    /**
     * 视频每日增量：按日期范围统计每天新增视频数（按 create_time 日期分组）
     */
    List<DailyIncrementVo> selectDailyIncrement(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * 今日上传视频数
     */
    Long selectTodayVideoCount();

    /**
     * 分类每日视频增量：按日期和分类两个维度统计每天每个分类下的新增视频数
     */
    List<CategoryDailyIncrementVo> selectCategoryDailyIncrement(@Param("startDate") LocalDate startDate,
                                                                 @Param("endDate") LocalDate endDate);
}
