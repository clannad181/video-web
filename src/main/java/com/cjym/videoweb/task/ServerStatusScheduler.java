package com.cjym.videoweb.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cjym.videoweb.constant.BaseConstant;
import com.cjym.videoweb.constant.MessageConstant;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.model.vo.SystemInfoVo;
import com.cjym.videoweb.service.MessageService;
import com.cjym.videoweb.service.SystemService;
import com.cjym.videoweb.service.WebConfigService;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * 服务器运行状态告警定时任务
 * <p>
 * 每 5 分钟检测 CPU/内存/磁盘使用率，超过阈值时给所有管理员发送告警消息；
 * 同一指标告警后 30 分钟内不重复发送（Redis setIfAbsent 去重），阈值走 web_config 配置，缺失时用默认值兜底。
 */
@Component
@Slf4j
public class ServerStatusScheduler {

    @Resource
    private SystemService systemService;

    @Resource
    private WebConfigService webConfigService;

    @Resource
    private MessageService messageService;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 每 5 分钟检测一次服务器运行状态
     */
    //todo 待完善
//    @Scheduled(fixedDelay = 5 * 60 * 1000L)
    public void checkServerStatus() {
        try {
            SystemInfoVo info = systemService.getSystemInfo();
            double cpuThreshold = resolveThreshold(BaseConstant.MONITOR_CPU_THRESHOLD, MessageConstant.DEFAULT_CPU_THRESHOLD);
            double memoryThreshold = resolveThreshold(BaseConstant.MONITOR_MEMORY_THRESHOLD, MessageConstant.DEFAULT_MEMORY_THRESHOLD);
            double diskThreshold = resolveThreshold(BaseConstant.MONITOR_DISK_THRESHOLD, MessageConstant.DEFAULT_DISK_THRESHOLD);
            // CPU 使用率
            if (info.getSystemCpuLoad() != null && info.getSystemCpuLoad() > cpuThreshold) {
                alert("cpu", "CPU", info.getSystemCpuLoad(), cpuThreshold);
            }
            // 系统内存使用率
            if (info.getSystemMemoryUsage() != null && info.getSystemMemoryUsage() > memoryThreshold) {
                alert("memory", "内存", info.getSystemMemoryUsage(), memoryThreshold);
            }
            // 磁盘使用率：任一挂载点超阈值即告警
            if (info.getDiskInfoList() != null) {
                info.getDiskInfoList().stream()
                        .filter(d -> d.getUsage() != null && d.getUsage() > diskThreshold)
                        .findFirst()
                        .ifPresent(d -> alert("disk", "磁盘", d.getUsage(), diskThreshold));
            }
        } catch (Exception e) {
            log.error("服务器状态检测失败", e);
        }
    }

    /**
     * 超阈值告警：Redis 去重通过后给所有管理员发送消息
     */
    private void alert(String metric, String metricName, double usage, double threshold) {
        String redisKey = MessageConstant.MONITOR_ALERT_REDIS_KEY_PREFIX + metric;
        Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "1",
                Duration.ofMinutes(MessageConstant.MONITOR_ALERT_INTERVAL_MINUTES));
        if (!Boolean.TRUE.equals(acquired)) {
            return;
        }
        List<Long> adminIds = listAdminIds();
        if (adminIds.isEmpty()) {
            return;
        }
        String content = String.format(MessageConstant.CONTENT_SERVER_STATUS, metricName, usage, threshold);
        messageService.createBatch(adminIds, MessageConstant.TYPE_SERVER_STATUS,
                MessageConstant.TITLE_SERVER_STATUS, content, null, null);
        log.warn("服务器状态告警已发送[指标={},使用率={}%,阈值={}%,管理员数={}]",
                metricName, usage, threshold, adminIds.size());
    }

    /**
     * 查询所有启用状态的管理员用户ID（type=1 且 status=0）
     */
    private List<Long> listAdminIds() {
        return userInfoMapper.selectList(new LambdaQueryWrapper<UserInfo>()
                        .select(UserInfo::getId)
                        .eq(UserInfo::getType, BaseConstant.USER_TYPE_ADMIN)
                        .eq(UserInfo::getStatus, 0))
                .stream()
                .map(UserInfo::getId)
                .toList();
    }

    /**
     * 读取 web_config 中的阈值配置（%），缺失或非法时使用默认值
     */
    private double resolveThreshold(String key, double defaultThreshold) {
        String value = webConfigService.getConfigValue(BaseConstant.WEB_CONFIG_CATEGORY_MONITOR, key);
        if (StringUtils.isEmpty(value)) {
            return defaultThreshold;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            log.warn("服务器监控阈值配置非法[{}={}]，使用默认值 {}", key, value, defaultThreshold);
            return defaultThreshold;
        }
    }
}
