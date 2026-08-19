package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "举报记录（管理端列表）")
public class ReportVo {

    @Schema(description = "举报ID")
    private Long id;

    @Schema(description = "被举报视频ID")
    private Long videoId;

    @Schema(description = "被举报视频avid")
    private String videoAvid;

    @Schema(description = "被举报视频标题")
    private String videoTitle;

    @Schema(description = "被举报剧集ID")
    private Long episodeId;

    @Schema(description = "被举报剧集avid")
    private String episodeAvid;

    @Schema(description = "被举报剧集标题")
    private String episodeTitle;

    @Schema(description = "举报人用户ID")
    private Long reporterId;

    @Schema(description = "举报人昵称")
    private String reporterNickName;

    @Schema(description = "举报类型：1-色情低俗，2-政治敏感，3-暴恐血腥，4-虚假信息，5-侵权抄袭，6-恶意引战，7-其他")
    private Integer reportType;

    @Schema(description = "举报补充说明")
    private String reason;

    @Schema(description = "处理状态：0-待处理，1-已受理（确认违规），2-已驳回（未违规）")
    private Integer status;

    @Schema(description = "处理人ID（管理员）")
    private Long handlerId;

    @Schema(description = "处理人昵称")
    private String handlerNickName;

    @Schema(description = "处理备注")
    private String handlerNote;

    @Schema(description = "处理时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;

    @Schema(description = "举报时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
