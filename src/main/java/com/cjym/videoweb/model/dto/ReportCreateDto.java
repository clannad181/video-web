package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "前台举报稿件请求")
public class ReportCreateDto {

    @Schema(description = "被举报视频avid（必填，如 av1a2b3C）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String videoUid;

    @Schema(description = "被举报剧集avid（可选，不传表示举报整个视频，如 avXyz789）")
    private String episodeUid;

    @Schema(description = "举报类型：1-色情低俗，2-政治敏感，3-暴恐血腥，4-虚假信息，5-侵权抄袭，6-恶意引战，7-其他", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer reportType;

    @Schema(description = "举报补充说明（可选，最长500字）")
    private String reason;
}
