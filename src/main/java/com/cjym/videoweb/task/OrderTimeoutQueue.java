package com.cjym.videoweb.task;

import com.cjym.videoweb.constant.OrderConstant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 订单超时延迟队列（Redisson RDelayedQueue）
 * <p>
 * 提供订单超时任务的入队与移除：下单时按支付截止时长延迟投递，到期后由
 * OrderTimeoutConsumer 消费并关闭订单；Redis 不可用时静默降级，由
 * OrderTimeoutScheduler 定时扫描兜底。
 */
@Component
@Slf4j
public class OrderTimeoutQueue {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 订单入队：按支付截止时长延迟投递（Redis 异常时静默降级，兜底扫描仍会关闭订单）
     */
    public void offer(String orderNo, LocalDateTime expireTime) {
        if (orderNo == null || expireTime == null) {
            return;
        }
        long delayMillis = Duration.between(LocalDateTime.now(), expireTime).toMillis();
        if (delayMillis <= 0) {
            return;
        }
        try {
            RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(blockingQueue());
            delayedQueue.offer(orderNo, delayMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("订单超时任务入队失败[orderNo={}]，将由兜底扫描关闭", orderNo, e);
        }
    }

    /**
     * 从队列移除任务：订单已支付或用户主动取消后调用，避免到期后无效消费
     */
    public void remove(String orderNo) {
        if (orderNo == null) {
            return;
        }
        try {
            RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(blockingQueue());
            delayedQueue.remove(orderNo);
        } catch (Exception e) {
            log.error("订单超时任务移除失败[orderNo={}]", orderNo, e);
        }
    }

    private RBlockingQueue<String> blockingQueue() {
        return redissonClient.getBlockingQueue(OrderConstant.ORDER_TIMEOUT_QUEUE_KEY);
    }
}
