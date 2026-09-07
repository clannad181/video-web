package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "标记消息已读请求")
public class MessageReadDto {

    @Schema(description = "消息avid", requiredMode = Schema.RequiredMode.REQUIRED)
    private String messageUid;
}
