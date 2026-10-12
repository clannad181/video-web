package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "创建订单结果")
public class OrderCreateResultVo {

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "订单金额（元）")
    private BigDecimal amount;

    @Schema(description = "订单支付截止时间（超时未支付自动关闭）")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
}
