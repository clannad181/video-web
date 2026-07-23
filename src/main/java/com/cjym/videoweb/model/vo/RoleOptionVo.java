package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色选项")
public class RoleOptionVo {

    @Schema(description = "角色id")
    private Long id;

    @Schema(description = "角色名称")
    private String name;
}
