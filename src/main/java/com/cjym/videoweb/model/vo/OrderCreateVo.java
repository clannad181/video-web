package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "前台创建大会员充值订单请求")
public class OrderCreateVo {

    @Schema(description = "充值月数（1~120，必须为 web_config 已配置套餐的时长）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer durationMonths;
}
