package com.cjym.videoweb.service.pay;

import com.cjym.videoweb.model.entity.OrderInfo;

import java.util.Map;

/**
 * 支付渠道适配器：统一各支付渠道的创建支付与回调处理能力
 * <p>
 * 后续接入新支付方式（如微信支付）时，新增实现类并注册到
 * {@link PaymentAdapterRegistry} 即可，订单与开通大会员流程无需改动。
 */
public interface PaymentAdapter {

    /**
     * 支付方式（对应 order_info.pay_method：1-支付宝，2-微信…）
     */
    Integer getPayMethod();

    /**
     * 渠道标识（小写字符串，对应统一回调路径 /client/pay/{channel}/notify|return，如 alipay）
     */
    String getChannel();

    /**
     * 支付完成后的前端结果页地址（同步跳转目标）
     */
    String getReturnUrl();

    /**
     * 异步通知地址（支付渠道服务器回调，本服务公网可达）
     */
    String getNotifyUrl();

    /**
     * 创建支付页面（返回自动跳转收银台的 HTML 表单）
     *
     * @param order     待支付订单
     * @param notifyUrl 异步通知地址（支付渠道服务器回调）
     * @param returnUrl 支付完成同步跳转地址（浏览器跳转）
     */
    String createPaymentPage(OrderInfo order, String notifyUrl, String returnUrl);

    /**
     * 校验支付渠道异步通知的签名
     */
    boolean verifyNotify(Map<String, String> params);

    /**
     * 从异步通知参数中解析业务订单号
     */
    String extractOrderNo(Map<String, String> params);

    /**
     * 从异步通知参数中解析支付金额（元，字符串形式）
     */
    String extractPayAmount(Map<String, String> params);
}
