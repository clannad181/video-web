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
     * 分页查询用户的观看历史（关联视频和剧集信息，每个视频仅保留最新观看的一条）
     */
    IPage<WatchHistoryVo> selectWatchHistoryPage(Page<WatchHistoryVo> page,
                                                  @Param("userId") Long userId);

    /**
     * 观看历史分页统计（每个视频仅计一次，供分页插件直接调用）
     */
    Long selectWatchHistoryPage_COUNT(@Param("userId") Long userId);

    /**
     * 分页查询用户观看历史的月份列表（按月去重倒序，支持关键字和时间范围过滤，供分组接口使用）
     */
    IPage<LocalDate> selectHistoryMonthPage(Page<LocalDate> page,
                                            @Param("userId") Long userId,
                                            @Param("keyword") String keyword,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * 观看历史月份分页统计（月份去重数，供分页插件直接调用）
     */
    Long selectHistoryMonthPage_COUNT(@Param("userId") Long userId,
                                      @Param("keyword") String keyword,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    /**
     * 按月份列表查询用户的观看历史记录（关联视频和剧集信息，按观看时间倒序）
     */
    List<WatchHistoryVo> selectHistoryByMonths(@Param("userId") Long userId,
                                               @Param("keyword") String keyword,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate,
                                               @Param("months") List<LocalDate> months);

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
