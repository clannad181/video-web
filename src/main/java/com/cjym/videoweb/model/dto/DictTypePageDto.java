package com.cjym.videoweb.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "字典类型分页记录")
public class DictTypePageDto {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "字典类型编码")
    private String typeCode;

    @Schema(description = "字典类型名称")
    private String typeName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "是否为系统内置字典")
    private Boolean isSystem;

    @Schema(description = "状态（true启用/false禁用）")
    private Boolean status;

    @Schema(description = "该类型下字典数据条数（未删除）")
    private Long dataCount;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
