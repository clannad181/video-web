package com.cjym.videoweb.service;

import com.cjym.videoweb.model.vo.DailyActiveVo;
import com.cjym.videoweb.model.vo.DailyIncrementVo;
import com.cjym.videoweb.model.vo.OverviewStatsVo;
import com.cjym.videoweb.model.vo.CategoryDailyIncrementVo;

import java.time.LocalDate;
import java.util.List;

public interface StatsService {

    /**
     * 总览统计：视频总数、总播放量、今日活跃用户数、评论总数
     */
    OverviewStatsVo getOverviewStats();

    /**
     * 每日增量统计：按日期范围统计每天新增视频数、评论数、播放量（无数据补0，合并返回）
     */
    List<DailyIncrementVo> getDailyIncrement(LocalDate startDate, LocalDate endDate);

    /**
     * 每日活跃用户统计：按日期范围统计每天活跃用户数（去重，无数据的日期补0）
     */
    List<DailyActiveVo> getDailyActiveUsers(LocalDate startDate, LocalDate endDate);

    /**
     * 分类每日视频增量：按分类×日期两个维度统计视频增量（无数据补0）
     */
    List<CategoryDailyIncrementVo> getCategoryDailyIncrement(LocalDate startDate, LocalDate endDate);
}
