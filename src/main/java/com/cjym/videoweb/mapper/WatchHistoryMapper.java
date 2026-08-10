package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.entity.WatchHistory;
import com.cjym.videoweb.model.vo.DailyActiveVo;
import com.cjym.videoweb.model.vo.DailyIncrementVo;
import com.cjym.videoweb.model.vo.WatchHistoryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 观看历史记录 Mapper
 */
@Mapper
public interface WatchHistoryMapper extends BaseMapper<WatchHistory> {

    /**
     * 分页查询用户的观看历史（关联视频和剧集信息）
     */
    IPage<WatchHistoryVo> selectWatchHistoryPage(Page<WatchHistoryVo> page,
                                                  @Param("userId") Long userId);

    /**
     * 按日期范围统计每日播放量
     */
    List<DailyIncrementVo> selectDailyPlayCount(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * 按日期范围统计每日活跃用户数（去重）
     */
    List<DailyActiveVo> selectDailyActiveUsers(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    /**
     * 今日活跃用户数（去重）
     */
    Long selectTodayActiveUserCount();

    /**
     * 今日总播放量
     */
    Long selectTodayPlayCount();
}
