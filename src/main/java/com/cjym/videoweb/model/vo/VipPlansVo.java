package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "大会员充值套餐与单月价格")
public class VipPlansVo {

    @Schema(description = "单月套餐（结构与套餐项一致：套餐名/时长/现价/原价）")
    @JsonProperty("vip_price_per_month")
    private VipPlanVo vipPricePerMonth;

    @Schema(description = "充值套餐列表")
    private List<VipPlanVo> plans;
}
