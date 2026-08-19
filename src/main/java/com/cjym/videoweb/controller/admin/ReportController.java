package com.cjym.videoweb.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.ReportHandleDto;
import com.cjym.videoweb.model.vo.ReportVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 稿件举报管理
 */
@RestController
@RequestMapping("/admin/report")
@Tag(name = "举报管理", description = "稿件举报处理与复核")
public class ReportController {

    @Resource
    private ReportService reportService;

    @GetMapping("/list")
    @Operation(summary = "分页查询举报列表", description = "分页查询举报记录（含视频/剧集/举报人/处理人信息），支持按处理状态、举报类型、视频标题、举报人昵称筛选")
    public R<IPage<ReportVo>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer reportType,
            @RequestParam(required = false) String videoTitle,
            @RequestParam(required = false) String reporterNickName) {
        return R.success(reportService.pageReportList(page, size, status, reportType, videoTitle, reporterNickName));
    }

    @PostMapping("/handle")
    @Operation(summary = "处理举报（复核）", description = "处理待处理举报：handleResult=1 已受理（确认违规，已发布视频同步下架），handleResult=2 已驳回（未违规）")
    public R<String> handle(@RequestBody ReportHandleDto dto) {
        reportService.handle(dto);
        return R.success("处理举报成功");
    }
}
