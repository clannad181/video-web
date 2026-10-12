package com.cjym.videoweb.service.pay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.cjym.videoweb.config.AlipayProperties;
import com.cjym.videoweb.constant.OrderConstant;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.model.entity.OrderInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 支付宝支付适配器（电脑网站支付 alipay.trade.page.pay）
 */
@Component
@Slf4j
public class AlipayPaymentAdapter implements PaymentAdapter {

    /** 渠道标识（对应统一回调路径 /client/pay/{channel}/notify|return） */
    public static final String CHANNEL = "alipay";

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private AlipayProperties alipayProperties;

    @Override
    public Integer getPayMethod() {
        return OrderConstant.PAY_METHOD_ALIPAY;
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    @Override
    public String getReturnUrl() {
        return alipayProperties.getReturnUrl();
    }

    @Override
    public String getNotifyUrl() {
        return alipayProperties.getNotifyUrl();
    }

    @Override
    public String createPaymentPage(OrderInfo order, String notifyUrl, String returnUrl) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);
        request.setBizContent("{" +
                "\"out_trade_no\":\"" + order.getOrderNo() + "\"," +
                "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                "\"total_amount\":\"" + order.getAmount() + "\"," +
                "\"subject\":\"" + order.getRemark() + "\"" +
                "}");
        try {
            return alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            log.error("创建支付宝支付页面失败[orderNo={}]", order.getOrderNo(), e);
            throw new GlobeException("支付宝下单失败，请稍后重试");
        }
    }

    @Override
    public boolean verifyNotify(Map<String, String> params) {
        try {
            // 校验通知来源应用与当前应用一致
            if (!alipayProperties.getAppid().equals(params.get("app_id"))) {
                log.warn("支付宝异步通知 app_id 不一致[app_id={}]", params.get("app_id"));
                return false;
            }
            return AlipaySignature.rsaCheckV1(params, alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(), alipayProperties.getSignType());
        } catch (AlipayApiException e) {
            log.error("支付宝异步通知验签异常", e);
            return false;
        }
    }

    @Override
    public String extractOrderNo(Map<String, String> params) {
        return params.get("out_trade_no");
    }

    @Override
    public String extractPayAmount(Map<String, String> params) {
        return params.get("total_amount");
    }
}
