package com.cjym.videoweb.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@TableName(schema = "public")
@Data
@Schema(description = "字典类型")
public class DictType {

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "字典类型编码（唯一）")
    private String typeCode;

    @Schema(description = "字典类型名称（唯一）")
    private String typeName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "是否为系统内置字典（系统内置不可删除、不可修改编码）")
    private Boolean isSystem;

    @Schema(description = "状态（true启用/false禁用）")
    private Boolean status;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "更新人")
    @TableField(fill = FieldFill.UPDATE)
    private Long updateUser;

    @Schema(description = "是否删除")
    @TableField(value = "is_delete", fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;
}
