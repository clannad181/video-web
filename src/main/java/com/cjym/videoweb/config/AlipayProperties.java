package com.cjym.videoweb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 支付宝支付配置属性（application.yml 中 pay.alipay 前缀）
 */
@Data
@ConfigurationProperties(prefix = "pay.alipay")
public class AlipayProperties {

    /**
     * 应用id
     */
    private String appid;

    /**
     * 支付宝网关地址（沙箱/正式环境不同）
     */
    private String gatewayHost;

    /**
     * 应用私钥
     */
    private String privateKey;

    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;

    /**
     * 签名算法
     */
    private String signType = "RSA2";

    /**
     * 字符编码
     */
    private String charset = "utf-8";

    /**
     * 支付成功同步跳转地址（前端支付结果页，支付完成后浏览器重定向并附带订单号参数）
     */
    private String returnUrl;

    /**
     * 支付成功异步回调地址
     */
    private String notifyUrl;
}
