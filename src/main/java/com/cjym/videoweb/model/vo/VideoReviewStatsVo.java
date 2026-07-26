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
}
