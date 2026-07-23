package com.cjym.videoweb.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "菜单信息（含父菜单名称）")
public class MenuDto {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "菜单标识")
    private String menuIdentification;

    @Schema(description = "菜单状态")
    private Boolean status;

    @Schema(description = "父菜单id")
    private Long parentId;

    @Schema(description = "排序")
    private Integer descSort;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "菜单类别")
    private String category;

    @Schema(description = "父菜单名称")
    private String parentMenuName;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "子菜单列表")
    private List<MenuDto> children = new ArrayList<>();
}
