package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "评论审核请求")
public class CommentReviewDto {

    @Schema(description = "评论ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commentId;

    @Schema(description = "审核结果：0-通过（status变为正常），2-拒绝（status变为运营屏蔽）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer reviewStatus;
}
