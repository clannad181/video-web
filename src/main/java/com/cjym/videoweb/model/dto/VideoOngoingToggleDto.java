package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "切换连载状态请求")
public class VideoOngoingToggleDto {

    @Schema(description = "视频avid")
    private String videoUid;
}
