package com.cjym.videoweb.constant;

import java.math.BigDecimal;

public class OrderConstant {

    /** 业务类型：大会员充值 */
    public static final int BIZ_TYPE_VIP_RECHARGE = 1;

    /** 订单状态：待支付 */
    public static final int STATUS_PENDING_PAY = 0;
    /** 订单状态：已支付（支付接入后使用） */
    public static final int STATUS_PAID = 1;
    /** 订单状态：已取消 */
    public static final int STATUS_CANCELLED = 2;
    /** 订单状态：已关闭（超时未支付） */
    public static final int STATUS_CLOSED = 3;

    /** 支付方式：支付宝 */
    public static final int PAY_METHOD_ALIPAY = 1;
    /** 支付方式：微信（预留，未接入） */
    public static final int PAY_METHOD_WECHAT = 2;

    /** 订单号前缀 */
    public static final String ORDER_NO_PREFIX = "on";

    /** 大会员月单价默认值（元/月），web_config 配置缺失时兜底 */
    public static final BigDecimal DEFAULT_VIP_MONTHLY_PRICE = new BigDecimal("25");

    /** 订单超时延迟队列 Key（Redisson 延迟队列，元素为订单号） */
    public static final String ORDER_TIMEOUT_QUEUE_KEY = "order:timeout:queue";

    /** 订单支付截止时长（分钟），超时未支付自动关闭 */
    public static final long ORDER_TIMEOUT_MINUTES = 30;
}
