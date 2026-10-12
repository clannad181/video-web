package com.cjym.videoweb.service.pay;

import com.cjym.videoweb.exception.GlobeException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 支付适配器注册表：按支付方式（order_info.pay_method）路由到对应适配器
 */
@Component
public class PaymentAdapterRegistry {

    private final Map<Integer, PaymentAdapter> adapterMap;

    private final Map<String, PaymentAdapter> channelMap;

    public PaymentAdapterRegistry(List<PaymentAdapter> adapters) {
        this.adapterMap = adapters.stream()
                .collect(Collectors.toMap(PaymentAdapter::getPayMethod, Function.identity()));
        this.channelMap = adapters.stream()
                .collect(Collectors.toMap(PaymentAdapter::getChannel, Function.identity()));
    }

    /**
     * 按支付方式获取适配器（发起支付时使用，未接入的支付方式抛异常）
     */
    public PaymentAdapter getAdapter(Integer payMethod) {
        PaymentAdapter adapter = adapterMap.get(payMethod);
        if (adapter == null) {
            throw new GlobeException("不支持的支付方式");
        }
        return adapter;
    }

    /**
     * 按渠道标识获取适配器（统一回调入口使用，未接入的渠道抛异常）
     */
    public PaymentAdapter getAdapterByChannel(String channel) {
        PaymentAdapter adapter = channelMap.get(channel);
        if (adapter == null) {
            throw new GlobeException("不支持的支付渠道");
        }
        return adapter;
    }
}
