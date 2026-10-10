package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "字典数据简要VO（前台下拉选择使用）")
public class DictDataSimpleVo {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "字典标签")
    private String label;

    @Schema(description = "字典值")
    private String value;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否默认项")
    private Boolean isDefault;
}
