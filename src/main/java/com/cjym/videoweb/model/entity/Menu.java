package com.cjym.videoweb.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@TableName(schema = "public")
@Data
@Schema(description = "菜单信息")
public class Menu {

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "菜单路由")
    private String path;

    @Schema(description = "页面标识")
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

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_delete", fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;
}
