package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 观看历史视频维度聚合统计（用户画像算法的输入）
 */
@Data
@Schema(description = "观看历史视频聚合统计")
public class WatchStatsVo {

    @Schema(description = "视频ID")
    private Long videoId;

    @Schema(description = "累计观看秒数")
    private Long totalSeconds;

    @Schema(description = "最近观看时间")
    private LocalDateTime lastWatchTime;
}
