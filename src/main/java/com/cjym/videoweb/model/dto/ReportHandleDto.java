package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理端处理举报请求")
public class ReportHandleDto {

    @Schema(description = "举报ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long reportId;

    @Schema(description = "处理结果：1-已受理（确认违规，视频下架），2-已驳回（未违规）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer handleResult;

    @Schema(description = "处理备注（可选，最长500字）")
    private String handlerNote;
}
