package com.cjym.videoweb.service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface PaymentService {

    /**
     * 发起支付：校验订单并生成支付页面 HTML（自动跳转收银台）
     */
    String createPaymentPage(String orderNo, Integer payMethod, HttpServletRequest request);

    /**
     * 处理支付渠道异步通知：验签、校验金额、更新订单、开通大会员（幂等），
     * 返回 "success" / "failure" 供支付渠道服务器解析
     */
    String handleNotify(String channel, Map<String, String> params);

    /**
     * 构建支付完成后的同步跳转地址（统一跳转：携带订单号重定向到渠道前端结果页）
     */
    String buildReturnRedirectUrl(String channel, Map<String, String> params);
}
