package com.cjym.videoweb.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(schema = "public")
@Schema(description = "网站配置")
public class WebConfig {

    @Schema(description = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "键")
    private String key;

    @Schema(description = "值")
    private String value;

    @Schema(description = "描述")
    private String description;

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
