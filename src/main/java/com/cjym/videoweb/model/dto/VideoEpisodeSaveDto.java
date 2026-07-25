package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "剧集编辑请求")
public class VideoEpisodeSaveDto {

    @Schema(description = "剧集id（编辑已有剧集时必填）")
    private Long id;

    @Schema(description = "集数序号")
    private Integer episodeNumber;

    @Schema(description = "剧集标题")
    private String title;

    @Schema(description = "剧集时长（秒）")
    private Long duration;

    @Schema(description = "播放地址")
    private String playUrl;

    @Schema(description = "排序")
    private Integer sort;
}
