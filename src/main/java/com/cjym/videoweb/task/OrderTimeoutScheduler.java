package com.cjym.videoweb.task;

import com.cjym.videoweb.service.OrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单超时关闭兜底定时任务
 * <p>
 * 订单超时关闭主路径为 Redis 延迟队列（OrderTimeoutQueue），本任务作为兜底：
 * 每 5 分钟扫描待支付且超过支付截止时间的订单并置为已关闭，
 * 保证 Redis 异常、入队失败或消费处理失败时订单仍能被关闭。
 */
@Component
@Slf4j
public class OrderTimeoutScheduler {

    @Resource
    private OrderService orderService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void closeTimeoutOrders() {
        try {
            int count = orderService.closeTimeoutOrders();
            if (count > 0) {
                log.info("订单超时关闭完成[关闭数量={}]", count);
            }
        } catch (Exception e) {
            log.error("订单超时关闭扫描异常", e);
        }
    }
}
