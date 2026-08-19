package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "前台发表评论请求")
public class CommentClientSaveDto {

    @Schema(description = "视频avid（必填，如 av1a2b3C）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String videoUid;

    @Schema(description = "剧集avid（必填，前台评论均为剧集级，如 avXyz789）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String episodeUid;

    @Schema(description = "父评论ID（回复时传，取自评论树接口返回的id字段；发表一级评论时不传或传0）")
    private Long parentId;

    @Schema(description = "评论正文（必填）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
