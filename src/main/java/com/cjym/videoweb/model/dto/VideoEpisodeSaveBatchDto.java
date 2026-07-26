package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "剧集批量保存请求")
public class VideoEpisodeSaveBatchDto {

    @Schema(description = "视频id（必填）")
    private Long videoId;

    @Schema(description = "剧集列表")
    private List<VideoEpisodeSaveDto> episodes;
}
