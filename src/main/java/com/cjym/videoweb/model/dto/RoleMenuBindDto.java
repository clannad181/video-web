package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "角色菜单绑定请求")
public class RoleMenuBindDto {

    @Schema(description = "角色id")
    private Long roleId;

    @Schema(description = "菜单id列表")
    private List<Long> menuIds;
}
