package com.cjym.videoweb.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝客户端配置：构建 AlipayClient 供支付适配器使用
 */
@Configuration
@EnableConfigurationProperties(AlipayProperties.class)
public class AlipayConfig {

    @Bean
    public AlipayClient alipayClient(AlipayProperties props) {
        return new DefaultAlipayClient(
                props.getGatewayHost(),
                props.getAppid(),
                props.getPrivateKey(),
                "json",
                props.getCharset(),
                props.getAlipayPublicKey(),
                props.getSignType());
    }
}
