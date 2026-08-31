package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "分类VO")
public class CategoryVo {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "分类名称（如番剧、国创）")
    private String name;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否显示")
    private Boolean isShow;

    @Schema(description = "是否为系统保留分类")
    private Boolean isSystem;

    @Schema(description = "状态（true启用/false禁用）")
    private Boolean status;

    @Schema(description = "SVG图标（前端展示分类图标使用）")
    private String svg;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
