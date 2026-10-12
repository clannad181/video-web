package com.cjym.videoweb.task;

import com.cjym.videoweb.service.VipService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 大会员到期失效定时任务
 * <p>
 * 每日凌晨 2 点扫描已到期的大会员用户，将其大会员状态失效（is_vip 置 false、清空到期时间），
 * 生效中的开通记录置为已到期，并发送站内消息通知；单用户处理失败不影响其他用户。
 */
@Component
@Slf4j
public class VipExpireScheduler {

    @Resource
    private VipService vipService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void expireVips() {
        log.info("大会员到期扫描开始");
        try {
            int count = vipService.processExpiredVips();
            log.info("大会员到期扫描完成[到期处理数={}]", count);
        } catch (Exception e) {
            log.error("大会员到期扫描异常", e);
        }
    }
}
