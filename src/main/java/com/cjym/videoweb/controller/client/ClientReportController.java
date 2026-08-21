package com.cjym.videoweb.controller.client;

import com.cjym.videoweb.model.dto.ReportCreateDto;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台稿件举报
 */
@RestController
@RequestMapping("/client/report")
@Tag(name = "稿件举报（前台）", description = "前台用户举报违规稿件接口")
public class ClientReportController {

    @Resource
    private ReportService reportService;

    /**
     * 举报稿件
     *
     * @param dto 举报请求体，videoUid 必填，episodeUid 可选
     */
    @PostMapping("/create")
    @Operation(summary = "举报稿件", description = "举报违规视频或剧集：videoUid为被举报视频avid（必填），episodeUid为被举报剧集avid（可选，不传表示举报整个视频）；同一用户对同一目标仅允许一条待处理举报")
    public R<String> report(@RequestBody ReportCreateDto dto) {
        reportService.createClient(dto);
        return R.success("举报成功");
    }
}
