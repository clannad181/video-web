package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "关注列表查询请求")
public class FollowListDto {

    @Schema(description = "页码，从1开始（默认1）")
    private Integer page;

    @Schema(description = "每页数量（默认10）")
    private Integer size;
}
