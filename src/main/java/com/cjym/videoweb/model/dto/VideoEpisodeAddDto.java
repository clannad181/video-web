package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "连载中视频添加剧集请求")
public class VideoEpisodeAddDto extends VideoEpisodeSaveDto {

    @Schema(description = "视频avid")
    private String videoUid;
}
