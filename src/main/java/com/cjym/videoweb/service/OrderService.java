package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.OrderPageDto;
import com.cjym.videoweb.model.entity.OrderInfo;
import com.cjym.videoweb.model.vo.OrderCancelVo;
import com.cjym.videoweb.model.vo.OrderCreateResultVo;
import com.cjym.videoweb.model.vo.OrderCreateVo;
import com.cjym.videoweb.result.R;

public interface OrderService extends IService<OrderInfo> {

    /**
     * 创建大会员充值订单（前台，金额按 web_config 配置的月单价计算）
     */
    R<OrderCreateResultVo> createVipOrder(OrderCreateVo vo);

    /**
     * 取消订单（前台，仅本人待支付订单可取消）
     */
    R<String> cancelOrder(OrderCancelVo vo);

    /**
     * 我的订单分页查询（前台）
     */
    IPage<OrderPageDto> pageMyOrders(Integer page, Integer size, Integer status);

    /**
     * 我的订单详情（前台，仅本人可查）
     */
    OrderPageDto getMyOrderDetail(String orderNo);

    /**
     * 订单分页查询（管理端）
     */
    IPage<OrderPageDto> pageOrders(Integer page, Integer size, String userUid, String orderNo,
                                   Integer bizType, Integer status);

    /**
     * 订单详情（管理端）
     */
    OrderPageDto getOrderDetail(String orderNo);

    /**
     * 关闭超时未支付的待支付订单（兜底定时任务调用，返回关闭数量）
     */
    int closeTimeoutOrders();

    /**
     * 按订单号关闭超时订单（Redis 延迟队列消费线程调用）：仅待支付且已过支付截止时间的订单会被关闭，幂等
     */
    void closeTimeoutOrder(String orderNo);
}
