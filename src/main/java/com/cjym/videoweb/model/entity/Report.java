package com.cjym.videoweb.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "report", schema = "public")
@Schema(description = "稿件举报")
public class Report {

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "被举报视频ID")
    private Long videoId;

    @Schema(description = "被举报剧集ID（为空表示举报整个视频）")
    private Long episodeId;

    @Schema(description = "举报人用户ID")
    private Long reporterId;

    @Schema(description = "举报类型：1-色情低俗，2-政治敏感，3-暴恐血腥，4-虚假信息，5-侵权抄袭，6-恶意引战，7-其他")
    private Integer reportType;

    @Schema(description = "举报补充说明")
    private String reason;

    @Schema(description = "处理状态：0-待处理，1-已受理（确认违规），2-已驳回（未违规）")
    private Integer status;

    @Schema(description = "处理人ID（管理员）")
    private Long handlerId;

    @Schema(description = "处理备注")
    private String handlerNote;

    @Schema(description = "处理时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;

    @Schema(description = "举报时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
