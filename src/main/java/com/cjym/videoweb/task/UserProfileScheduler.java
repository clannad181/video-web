package com.cjym.videoweb.task;

import com.cjym.videoweb.constant.UserProfileConstant;
import com.cjym.videoweb.mapper.UserProfileMapper;
import com.cjym.videoweb.mapper.WatchHistoryMapper;
import com.cjym.videoweb.service.UserProfileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户画像定时任务
 * <p>
 * 每日凌晨3点批量重建所有活跃用户的画像：
 * 先清理时间窗内已无观看记录用户的旧画像，再逐用户重算覆盖写入。
 */
@Component
@Slf4j
public class UserProfileScheduler {

    @Resource
    private UserProfileService userProfileService;

    @Resource
    private WatchHistoryMapper watchHistoryMapper;

    @Resource
    private UserProfileMapper userProfileMapper;

    /**
     * 每日凌晨3点批量重建用户画像（单用户失败不影响其他用户）
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void rebuildAllProfiles() {
        LocalDateTime since = LocalDateTime.now().minusDays(UserProfileConstant.PROFILE_WINDOW_DAYS);
        // 1. 清理时间窗内已无观看记录用户的旧画像，保持画像表与观看历史一致
        int removed = userProfileMapper.deleteInactiveProfiles(since);
        // 2. 重建所有活跃用户画像
        List<Long> userIds = watchHistoryMapper.selectProfileUserIds(since);
        log.info("用户画像定时重建开始[活跃用户数={},清理画像数={}]", userIds.size(), removed);
        int success = 0;
        for (Long userId : userIds) {
            try {
                userProfileService.rebuildProfile(userId);
                success++;
            } catch (Exception e) {
                log.error("用户画像重建失败[userId={}]", userId, e);
            }
        }
        log.info("用户画像定时重建完成[成功={},失败={}]", success, userIds.size() - success);
    }
}
