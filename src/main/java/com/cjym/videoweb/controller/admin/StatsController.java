package com.cjym.videoweb.controller.admin;

import com.cjym.videoweb.model.vo.DailyActiveVo;
import com.cjym.videoweb.model.vo.DailyIncrementVo;
import com.cjym.videoweb.model.vo.OverviewStatsVo;
import com.cjym.videoweb.model.vo.CategoryDailyIncrementVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/stats")
@Tag(name = "统计看板", description = "运营统计数据")
public class StatsController {

    @Resource
    private StatsService statsService;

    @GetMapping("/overview")
    @Operation(summary = "总览统计", description = "统计视频总数、总播放量、今日活跃用户数、评论总数")
    public R<OverviewStatsVo> overview() {
        return R.success(statsService.getOverviewStats());
    }

    @GetMapping("/daily-increment")
    @Operation(summary = "每日增量统计", description = "按日期范围统计每天新增视频数、评论数和总播放量，合并返回")
    public R<List<DailyIncrementVo>> dailyIncrement(
            @Parameter(description = "起始日期", required = true, example = "2026-08-01")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,
            @Parameter(description = "结束日期", required = true, example = "2026-08-07")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime) {
        return R.success(statsService.getDailyIncrement(startTime, endTime));
    }

    @GetMapping("/active-users")
    @Operation(summary = "每日活跃用户统计", description = "按日期范围统计每天活跃用户数（去重），基于观看历史记录")
    public R<List<DailyActiveVo>> activeUsers(
            @Parameter(description = "起始日期", required = true, example = "2026-08-01")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,
            @Parameter(description = "结束日期", required = true, example = "2026-08-07")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime) {
        return R.success(statsService.getDailyActiveUsers(startTime, endTime));
    }

    @GetMapping("/category-daily-increment")
    @Operation(summary = "分类每日视频增量", description = "按分类和日期两个维度统计每天每个分类下的新增视频数（无数据补0）")
    public R<List<CategoryDailyIncrementVo>> categoryDailyIncrement(
            @Parameter(description = "起始日期", required = true, example = "2026-08-01")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,
            @Parameter(description = "结束日期", required = true, example = "2026-08-07")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime) {
        return R.success(statsService.getCategoryDailyIncrement(startTime, endTime));
    }
}
