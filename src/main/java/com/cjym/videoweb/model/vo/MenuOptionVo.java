package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "菜单选项")
public class MenuOptionVo {

    @Schema(description = "菜单id")
    private Long id;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "父菜单id")
    private Long parentId;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "子菜单列表")
    private List<MenuOptionVo> children = new ArrayList<>();

    public MenuOptionVo(Long id, String menuName, Long parentId) {
        this.id = id;
        this.menuName = menuName;
        this.parentId = parentId;
    }

    public MenuOptionVo(Long id, String menuName, Long parentId, String icon) {
        this.id = id;
        this.menuName = menuName;
        this.parentId = parentId;
        this.icon = icon;
    }
}
