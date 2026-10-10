package com.cjym.videoweb.service.impl;

import com.cjym.videoweb.model.vo.SystemInfoVo;
import com.cjym.videoweb.service.SystemService;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SystemServiceImpl implements SystemService {

    @Override
    public SystemInfoVo getSystemInfo() {
        SystemInfoVo vo = new SystemInfoVo();

        // ========== CPU ==========
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        vo.setCpuCores(osBean.getAvailableProcessors());
        vo.setSystemCpuLoad(roundPercent(osBean.getSystemCpuLoad()));
        vo.setProcessCpuLoad(roundPercent(osBean.getProcessCpuLoad()));
        vo.setSystemLoadAverage(roundTwo(osBean.getSystemLoadAverage()));

        // ========== JVM内存（MB） ==========
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        vo.setHeapUsed(bytesToMB(heap.getUsed()));
        vo.setHeapMax(bytesToMB(heap.getMax()));
        if (heap.getMax() > 0) {
            vo.setHeapUsage(roundPercent((double) heap.getUsed() / heap.getMax()));
        } else {
            vo.setHeapUsage(0.0);
        }
        vo.setNonHeapUsed(bytesToMB(memoryBean.getNonHeapMemoryUsage().getUsed()));

        // ========== 系统内存（MB） ==========
        vo.setSystemTotalMemory(bytesToMB(osBean.getTotalMemorySize()));
        vo.setSystemFreeMemory(bytesToMB(osBean.getFreeMemorySize()));
        long usedMem = osBean.getTotalMemorySize() - osBean.getFreeMemorySize();
        vo.setSystemUsedMemory(bytesToMB(usedMem));
        if (osBean.getTotalMemorySize() > 0) {
            vo.setSystemMemoryUsage(roundPercent((double) usedMem / osBean.getTotalMemorySize()));
        } else {
            vo.setSystemMemoryUsage(0.0);
        }

        // ========== JVM信息 ==========
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        vo.setJavaVersion(System.getProperty("java.version"));
        vo.setJavaVendor(System.getProperty("java.vendor"));
        vo.setOsName(osBean.getName());
        vo.setOsArch(osBean.getArch());
        vo.setJvmStartTime(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(runtimeBean.getStartTime()),
                ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        long uptimeMillis = runtimeBean.getUptime();
        vo.setJvmUptime(formatDuration(uptimeMillis));

        // ========== 线程 ==========
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        vo.setThreadCount(threadBean.getThreadCount());
        vo.setDaemonThreadCount(threadBean.getDaemonThreadCount());
        vo.setPeakThreadCount(threadBean.getPeakThreadCount());

        // ========== 磁盘 ==========
        vo.setDiskInfoList(getDiskInfoList());

        return vo;
    }

    // ==================== 工具方法 ====================

    /**
     * 百分比四舍五入保留两位小数，负数返回 0
     */
    private Double roundPercent(double value) {
        if (value < 0) return 0.0;
        return Math.round(value * 10000.0) / 100.0;
    }

    /**
     * 保留两位小数
     */
    private Double roundTwo(double value) {
        if (value < 0) return 0.0;
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * 字节转MB
     */
    private Long bytesToMB(long bytes) {
        return bytes / 1024 / 1024;
    }

    /**
     * 毫秒转可读时长
     */
    private String formatDuration(long millis) {
        long days = millis / 86400000;
        long hours = (millis % 86400000) / 3600000;
        long minutes = (millis % 3600000) / 60000;
        long seconds = (millis % 60000) / 1000;
        if (days > 0) {
            return String.format("%d天 %d小时 %d分钟 %d秒", days, hours, minutes, seconds);
        }
        return String.format("%d小时 %d分钟 %d秒", hours, minutes, seconds);
    }

    /**
     * 获取磁盘分区列表
     */
    private List<SystemInfoVo.DiskInfoVo> getDiskInfoList() {
        List<SystemInfoVo.DiskInfoVo> list = new ArrayList<>();
        File[] roots = File.listRoots();
        if (roots != null) {
            for (File root : roots) {
                SystemInfoVo.DiskInfoVo disk = new SystemInfoVo.DiskInfoVo();
                disk.setMount(root.getPath());
                disk.setTotal(root.getTotalSpace() / 1024 / 1024 / 1024);
                disk.setFree(root.getFreeSpace() / 1024 / 1024 / 1024);
                disk.setUsed(disk.getTotal() - disk.getFree());
                if (disk.getTotal() > 0) {
                    disk.setUsage(roundPercent((double) disk.getUsed() / disk.getTotal()));
                } else {
                    disk.setUsage(0.0);
                }
                list.add(disk);
            }
        }
        return list;
    }
}
