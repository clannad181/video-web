package com.cjym.videoweb.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "字典数据分页记录")
public class DictDataPageDto {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "所属字典类型编码")
    private String typeCode;

    @Schema(description = "字典标签")
    private String label;

    @Schema(description = "字典值")
    private String value;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否默认项")
    private Boolean isDefault;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态（true启用/false禁用）")
    private Boolean status;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
