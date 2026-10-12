package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发起支付请求参数
 */
@Data
@Schema(description = "发起支付请求参数")
public class PayOrderVo {

    @Schema(description = "订单号")
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @Schema(description = "支付方式（1-支付宝，2-微信预留）")
    @NotNull(message = "支付方式不能为空")
    private Integer payMethod;
}
