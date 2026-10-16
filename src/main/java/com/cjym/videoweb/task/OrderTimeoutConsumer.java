package com.cjym.videoweb.task;

import com.cjym.videoweb.constant.OrderConstant;
import com.cjym.videoweb.service.OrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 订单超时延迟队列消费者
 * <p>
 * 应用启动完成后拉起后台线程，阻塞获取到期订单号并关闭订单；处理失败仅记录日志，
 * 由 OrderTimeoutScheduler 定时扫描兜底。与 OrderTimeoutQueue 职责分离，
 * 避免 OrderService 与队列组件之间形成 Bean 循环依赖。
 */
@Component
@Slf4j
public class OrderTimeoutConsumer {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private OrderService orderService;

    /**
     * 应用启动完成后拉起消费线程，阻塞等待到期订单号并执行关闭
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startConsumer() {
        Thread consumer = new Thread(this::consumeLoop, "order-timeout-consumer");
        consumer.setDaemon(true);
        consumer.start();
        log.info("订单超时延迟队列消费线程已启动");
    }

    /**
     * 消费循环：阻塞获取到期订单号并关闭订单，处理失败仅记录日志（由兜底扫描补偿）
     */
    private void consumeLoop() {
        RBlockingQueue<String> queue = redissonClient.getBlockingQueue(OrderConstant.ORDER_TIMEOUT_QUEUE_KEY);
        while (true) {
            String orderNo;
            try {
                orderNo = queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("订单超时延迟队列消费线程已停止");
                return;
            } catch (Exception e) {
                log.error("订单超时延迟队列消费异常，稍后重试", e);
                sleepQuietly(5000);
                continue;
            }
            try {
                orderService.closeTimeoutOrder(orderNo);
            } catch (Exception e) {
                log.error("订单超时关闭处理失败[orderNo={}]，将由兜底扫描补偿", orderNo, e);
            }
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
