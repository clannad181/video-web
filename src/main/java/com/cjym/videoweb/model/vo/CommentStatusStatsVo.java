package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 评论状态统计
 */
@Data
@Schema(description = "评论状态统计")
public class CommentStatusStatsVo {

    @Schema(description = "正常评论数")
    private Long normalCount;

    @Schema(description = "用户删除评论数")
    private Long userDeletedCount;

    @Schema(description = "运营屏蔽评论数")
    private Long adminBlockedCount;

    @Schema(description = "运营屏蔽评论数")
    private Long pendingReviewCount;

    @Schema(description = "评论总数")
    private Long totalCount;
}
