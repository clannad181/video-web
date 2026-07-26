package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "发表评论请求")
public class CommentSaveDto {

    @Schema(description = "视频ID（必填）")
    private Long videoId;

    @Schema(description = "父评论ID（回复时传，发表一级评论时不传或传0）")
    private Long parentId;

    @Schema(description = "评论正文（必填）")
    private String content;
}
