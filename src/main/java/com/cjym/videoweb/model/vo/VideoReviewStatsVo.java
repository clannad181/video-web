package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "视频审核统计")
public class VideoReviewStatsVo {

    @Schema(description = "待审核数（所有剧集均未审核）")
    private Long pendingCount;

    @Schema(description = "部分审核数（部分剧集已审核）")
    private Long partialCount;

    @Schema(description = "审核完成数（所有剧集均已审核）")
    private Long completedCount;

    @Schema(description = "草稿数（video_status = 0）")
    private Long draftCount;

    @Schema(description = "待发布数（video_status = 1）")
    private Long pendingPublishCount;

    @Schema(description = "已发布数（video_status = 2）")
    private Long publishedCount;

    @Schema(description = "已下架数（video_status = 3）")
    private Long removedCount;

    @Schema(description = "今日新增视频数")
    private Long todayNewCount;

    @Schema(description = "较昨日新增变化（正为升，负为降）")
    private Long todayNewDiff;

    @Schema(description = "今日待审核视频数")
    private Long todayPendingCount;

    @Schema(description = "较昨日待审核变化（正为升，负为降）")
    private Long todayPendingDiff;

    // ===== 以下为内部计算用字段，不暴露给前端 =====

    @Schema(hidden = true)
    private Long yesterdayNewCount;

    @Schema(hidden = true)
    private Long yesterdayPendingCount;
}
