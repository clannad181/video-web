package com.cjym.videoweb.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "订单分页列表项/详情")
public class OrderPageDto {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "下单用户ID")
    private Long userId;

    @Schema(description = "用户uid")
    private String userUid;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "用户昵称")
    private String nikeName;

    @Schema(description = "业务类型：1-大会员充值")
    private Integer bizType;

    @Schema(description = "订单金额（元）")
    private BigDecimal amount;

    @Schema(description = "大会员充值月数（其他业务为 null）")
    private Integer durationMonths;

    @Schema(description = "订单状态：0-待支付，1-已支付，2-已取消，3-已关闭")
    private Integer status;

    @Schema(description = "支付方式：1-支付宝，2-微信（未支付为 null）")
    private Integer payMethod;

    @Schema(description = "支付时间（未支付为 null）")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    @Schema(description = "订单支付截止时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    @Schema(description = "订单备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
