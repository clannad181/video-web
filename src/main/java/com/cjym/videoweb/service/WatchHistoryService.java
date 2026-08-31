package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.WatchHistorySaveDto;
import com.cjym.videoweb.model.dto.WatchHistorySearchDto;
import com.cjym.videoweb.model.entity.WatchHistory;
import com.cjym.videoweb.model.vo.DailyActiveVo;
import com.cjym.videoweb.model.vo.DailyIncrementVo;
import com.cjym.videoweb.model.vo.WatchHistoryGroupVo;
import com.cjym.videoweb.model.vo.WatchHistoryMonthVo;
import com.cjym.videoweb.model.vo.WatchHistoryVo;

import java.time.LocalDate;
import java.util.List;

/**
 * 观看历史记录 Service
 */
public interface WatchHistoryService extends IService<WatchHistory> {

    /**
     * 记录或更新观看历史（同一用户+视频+剧集存在则更新进度）
     */
    void record(WatchHistorySaveDto dto);

    /**
     * 播放时记录观看历史（观看进度记0秒，未登录不记录）
     */
    void recordPlay(Long videoId, Long episodeId);

    /**
     * 分页查询当前用户的观看历史
     */
    IPage<WatchHistoryVo> pageHistory(Integer page, Integer size);

    /**
     * 搜索当前用户的观看历史（按月份分组分页，每页 size 个月，月内数据完整；关键字聚合模糊匹配视频/剧集标题与UP主昵称，支持时间范围过滤）
     */
    IPage<WatchHistoryMonthVo> pageHistoryByMonth(WatchHistorySearchDto dto);

    /**
     * 按视频avid删除当前用户的观看历史（传剧集avid时仅删除该剧集的记录，不传则删除该视频的全部记录）
     */
    void deleteHistory(String videoUid, String episodeUid);

    /**
     * 清空当前用户的全部观看历史记录
     */
    void clearHistory();

    /**
     * 按日期范围统计每日播放量（无数据的日期补0）
     */
    List<DailyIncrementVo> getDailyPlayCount(LocalDate startDate, LocalDate endDate);

    /**
     * 按日期范围统计每日活跃用户数（无数据的日期补0）
     */
    List<DailyActiveVo> getDailyActiveUsers(LocalDate startDate, LocalDate endDate);
}
