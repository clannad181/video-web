package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "记录观看历史请求")
public class WatchHistorySaveDto {

    @Schema(description = "视频ID", required = true)
    private Long videoId;

    @Schema(description = "剧集ID（单集视频不传）")
    private Long episodeId;

    @Schema(description = "已观看秒数", required = true)
    private Long watchedSeconds;
}
