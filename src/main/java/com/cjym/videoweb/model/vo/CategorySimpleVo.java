package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分类精简信息（id/name/sort/svg），供前台分类导航及下拉选择等场景使用")
public class CategorySimpleVo {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "SVG图标（前端展示分类图标使用）")
    private String svg;
}
