package com.cjym.videoweb.controller.admin;

import com.cjym.videoweb.model.vo.DailyIncrementVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/stats")
@Tag(name = "统计看板", description = "运营统计数据")
public class StatsController {

    @Resource
    private StatsService statsService;

    @GetMapping("/daily-increment")
    @Operation(summary = "每日增量统计", description = "统计最近7天每天新增视频数和评论数，合并返回")
    public R<List<DailyIncrementVo>> dailyIncrement() {
        return R.success(statsService.getDailyIncrement());
    }
}
