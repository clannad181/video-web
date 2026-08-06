package com.cjym.videoweb.service;

import com.cjym.videoweb.model.vo.SystemInfoVo;

public interface SystemService {

    /**
     * 获取系统信息：CPU、内存、磁盘、JVM等资源使用情况
     */
    SystemInfoVo getSystemInfo();
}
