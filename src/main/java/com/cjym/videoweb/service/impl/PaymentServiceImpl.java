package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cjym.videoweb.constant.OrderConstant;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.OrderInfoMapper;
import com.cjym.videoweb.model.entity.OrderInfo;
import com.cjym.videoweb.service.PaymentService;
import com.cjym.videoweb.service.VipService;
import com.cjym.videoweb.service.pay.PaymentAdapter;
import com.cjym.videoweb.service.pay.PaymentAdapterRegistry;
import com.cjym.videoweb.utils.JWTUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private OrderInfoMapper orderInfoMapper;

    @Resource
    private VipService vipService;

    @Resource
    private PaymentAdapterRegistry paymentAdapterRegistry;

    @Override
    public String createPaymentPage(String orderNo, Integer payMethod, HttpServletRequest request) {
        Long userId = requireLoginUserId(request);
        if (StringUtils.isEmpty(orderNo)) {
            throw new GlobeException("订单号不能为空");
        }
        OrderInfo order = orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getOrderNo, orderNo.trim()));
        if (order == null) {
            throw new GlobeException("订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            throw new GlobeException("只能支付自己的订单");
        }
        if (!Integer.valueOf(OrderConstant.STATUS_PENDING_PAY).equals(order.getStatus())) {
            throw new GlobeException("当前订单状态不可支付");
        }
        if (order.getExpireTime() != null && !order.getExpireTime().isAfter(LocalDateTime.now())) {
            throw new GlobeException("订单已超时，请重新下单");
        }
        // 路由到对应支付渠道适配器
        PaymentAdapter adapter = paymentAdapterRegistry.getAdapter(payMethod);
        // 记录支付方式
        order.setPayMethod(payMethod);
        orderInfoMapper.updateById(order);
        // 异步通知地址与同步跳转地址由渠道适配器提供（各渠道独立配置）
        return adapter.createPaymentPage(order, adapter.getNotifyUrl(), adapter.getReturnUrl());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleNotify(String channel, Map<String, String> params) {
        try {
            log.info("支付异步通知[channel={}]", channel);
            PaymentAdapter adapter = paymentAdapterRegistry.getAdapterByChannel(channel);
            // 1. 验签
            if (!adapter.verifyNotify(params)) {
                log.warn("支付异步通知验签失败[channel={}]", channel);
                return "failure";
            }
            // 2. 解析订单号与金额
            String orderNo = adapter.extractOrderNo(params);
            String payAmount = adapter.extractPayAmount(params);
            if (StringUtils.isEmpty(orderNo)) {
                log.warn("支付异步通知缺少订单号[channel={}]", channel);
                return "failure";
            }
            OrderInfo order = orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                    .eq(OrderInfo::getOrderNo, orderNo.trim()));
            if (order == null) {
                log.warn("支付异步通知订单不存在[orderNo={}]", orderNo);
                return "failure";
            }
            // 3. 幂等：已支付直接应答成功，避免重复开通
            if (Integer.valueOf(OrderConstant.STATUS_PAID).equals(order.getStatus())) {
                return "success";
            }
            if (!Integer.valueOf(OrderConstant.STATUS_PENDING_PAY).equals(order.getStatus())) {
                log.warn("支付异步通知订单状态异常[orderNo={},status={}]", orderNo, order.getStatus());
                return "failure";
            }
            // 4. 金额校验（与订单金额一致，防篡改）
            if (StringUtils.isEmpty(payAmount)
                    || new BigDecimal(payAmount).compareTo(order.getAmount()) != 0) {
                log.error("支付异步通知金额不一致[orderNo={},notify={},order={}]",
                        orderNo, payAmount, order.getAmount());
                return "failure";
            }
            // 5. 订单置为已支付（支付方式以适配器映射为准）
            order.setStatus(OrderConstant.STATUS_PAID);
            order.setPayMethod(adapter.getPayMethod());
            order.setPayTime(LocalDateTime.now());
            orderInfoMapper.updateById(order);
            // 6. 开通大会员（大会员充值订单）
            if (OrderConstant.BIZ_TYPE_VIP_RECHARGE == order.getBizType()) {
                if (order.getDurationMonths() == null) {
                    log.error("大会员充值订单缺少充值月数[orderNo={}]", orderNo);
                    return "failure";
                }
                vipService.openVipForOrder(order.getUserId(), order.getDurationMonths());
            }
            log.info("支付成功[orderNo={},amount={}]", orderNo, payAmount);
            return "success";
        } catch (Exception e) {
            log.error("支付异步通知处理异常", e);
            return "failure";
        }
    }

    @Override
    public String buildReturnRedirectUrl(String channel, Map<String, String> params) {
        PaymentAdapter adapter = paymentAdapterRegistry.getAdapterByChannel(channel);
        String orderNo = adapter.extractOrderNo(params);
        String returnUrl = adapter.getReturnUrl();
        String separator = returnUrl.contains("?") ? "&" : "?";
        return returnUrl + separator + "orderNo=" + (orderNo == null ? "" : orderNo);
    }


    /**
     * 解析当前登录用户id（未登录抛异常）
     */
    private Long requireLoginUserId(HttpServletRequest request) {
        try {
            String token = request.getHeader("authorization");
            if (StringUtils.isEmpty(token)) {
                throw new GlobeException("请先登录");
            }
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = JWTUtil.getUserId(token);
            if (userId == null) {
                throw new GlobeException("请先登录");
            }
            return userId;
        } catch (GlobeException e) {
            throw e;
        } catch (Exception e) {
            throw new GlobeException("请先登录");
        }
    }
}
