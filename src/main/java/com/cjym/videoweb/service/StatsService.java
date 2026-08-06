package com.cjym.videoweb.service;

import com.cjym.videoweb.model.vo.DailyIncrementVo;

import java.util.List;

public interface StatsService {

    /**
     * 每日增量统计：最近7天每天新增视频数和评论数（无数据补0，合并返回）
     */
    List<DailyIncrementVo> getDailyIncrement();
}
