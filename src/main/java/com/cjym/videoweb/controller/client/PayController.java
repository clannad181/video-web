package com.cjym.videoweb.controller.client;

import com.cjym.videoweb.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * 支付统一回调入口（客户端）
 * <p>
 * 按渠道标识（channel，如 alipay）路由到对应支付适配器，
 * 由支付渠道服务器/浏览器回调，无需登录，已在拦截器放行。
 * 后期接入新支付方式时无需新增回调接口，渠道自行扩展即可。
 */
@RestController
@RequestMapping("/client/pay")
@Tag(name = "支付回调", description = "各支付渠道统一异步通知与同步跳转入口（渠道服务器/浏览器调用，无需登录）")
@Slf4j
public class PayController {

    @Resource
    private PaymentService paymentService;

    @PostMapping("/{channel}/notify")
    @Operation(summary = "支付渠道异步通知", description = "按渠道路由：验签、校验金额、更新订单并开通大会员（幂等），返回纯文本 success/failure")
    public String notify(@PathVariable String channel, @RequestParam Map<String, String> params) {
        log.info("收到支付异步通知[channel={}]", channel);
        return paymentService.handleNotify(channel, params);
    }

    @GetMapping("/{channel}/return")
    @Operation(summary = "支付渠道同步跳转", description = "支付完成后浏览器跳转：按渠道解析订单号，重定向到渠道配置的支付结果页")
    public void payReturn(@PathVariable String channel, @RequestParam Map<String, String> params,
                          HttpServletResponse response) throws IOException {
        response.sendRedirect(paymentService.buildReturnRedirectUrl(channel, params));
    }
}
