package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "视频新增/编辑请求")
public class VideoSaveDto {

    @Schema(description = "视频id（编辑时必填）")
    private Long id;

    @Schema(description = "视频标题")
    private String title;

    @Schema(description = "视频简介")
    private String description;

    @Schema(description = "分类id")
    private Long categoryId;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "剧集列表")
    private List<VideoEpisodeSaveDto> episodes;
}
