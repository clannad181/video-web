package com.cjym.videoweb.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 通用订单
 */
@Data
@TableName(value = "order_info", schema = "public")
@Schema(description = "通用订单")
public class OrderInfo {

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单号（on + Base62编码的雪花ID，唯一）")
    private String orderNo;

    @Schema(description = "下单用户ID（关联 user_info.id）")
    private Long userId;

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

    @Schema(description = "订单支付截止时间（超时未支付自动关闭）")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    @Schema(description = "订单备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "创建人（下单用户ID）")
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @Schema(description = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "更新人")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    @Schema(description = "是否删除（0-未删除，1-已删除）")
    @TableField(value = "is_delete", fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;
}
