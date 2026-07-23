package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "系统信息")
public class SystemInfoVo {

    // ========== CPU ==========
    @Schema(description = "CPU核心数")
    private Integer cpuCores;

    @Schema(description = "系统CPU使用率（%，0-100）")
    private Double systemCpuLoad;

    @Schema(description = "JVM进程CPU使用率（%，0-100）")
    private Double processCpuLoad;

    @Schema(description = "系统平均负载")
    private Double systemLoadAverage;

    // ========== JVM内存 ==========
    @Schema(description = "堆内存-已使用（MB）")
    private Long heapUsed;

    @Schema(description = "堆内存-最大（MB）")
    private Long heapMax;

    @Schema(description = "堆内存使用率（%）")
    private Double heapUsage;

    @Schema(description = "非堆内存-已使用（MB）")
    private Long nonHeapUsed;

    // ========== 系统内存 ==========
    @Schema(description = "系统总内存（MB）")
    private Long systemTotalMemory;

    @Schema(description = "系统空闲内存（MB）")
    private Long systemFreeMemory;

    @Schema(description = "系统已用内存（MB）")
    private Long systemUsedMemory;

    @Schema(description = "系统内存使用率（%）")
    private Double systemMemoryUsage;

    // ========== JVM信息 ==========
    @Schema(description = "Java版本")
    private String javaVersion;

    @Schema(description = "Java供应商")
    private String javaVendor;

    @Schema(description = "JVM启动时间")
    private String jvmStartTime;

    @Schema(description = "JVM运行时长")
    private String jvmUptime;

    @Schema(description = "操作系统名称")
    private String osName;

    @Schema(description = "操作系统架构")
    private String osArch;

    // ========== 线程 ==========
    @Schema(description = "当前线程数")
    private Integer threadCount;

    @Schema(description = "守护线程数")
    private Integer daemonThreadCount;

    @Schema(description = "峰值线程数")
    private Integer peakThreadCount;

    // ========== 磁盘 ==========
    @Schema(description = "磁盘信息列表")
    private List<DiskInfoVo> diskInfoList;

    @Data
    @Schema(description = "磁盘信息")
    public static class DiskInfoVo {
        @Schema(description = "盘符")
        private String mount;

        @Schema(description = "文件系统类型")
        private String type;

        @Schema(description = "总空间（GB）")
        private Long total;

        @Schema(description = "可用空间（GB）")
        private Long free;

        @Schema(description = "已用空间（GB）")
        private Long used;

        @Schema(description = "使用率（%）")
        private Double usage;
    }
}
