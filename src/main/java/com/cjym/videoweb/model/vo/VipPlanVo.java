package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "大会员充值套餐")
public class VipPlanVo {

    @Schema(description = "套餐名称")
    private String name;

    @Schema(description = "套餐时长（月）")
    private Integer durationMonths;

    @Schema(description = "现价（元，下单金额）")
    private BigDecimal price;

    @Schema(description = "原价（元，用于前端划线展示；配置 autoOrig=true 时按月单价自动计算）")
    private BigDecimal originalPrice;

    @Schema(description = "原价是否按月单价自动计算（仅配置解析使用，不返回前端）")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Boolean autoOrig;
}
