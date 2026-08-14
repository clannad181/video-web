package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "关注UP主请求")
public class FollowSaveDto {

    @Schema(description = "被关注的UP主用户id")
    private Long followUserId;
}
