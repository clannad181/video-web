package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.OperationLogPageDto;
import com.cjym.videoweb.model.entity.OperationLog;
import com.cjym.videoweb.model.vo.IpRankVo;
import com.cjym.videoweb.model.vo.LogOverviewVo;
import com.cjym.videoweb.model.vo.OperationLogDetailVo;
import com.cjym.videoweb.model.vo.OperationTrendVo;
import com.cjym.videoweb.model.vo.HourlyActiveVo;

import java.util.List;

/**
 * 运行日志 Service
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 保存运行日志
     */
    void saveLog(OperationLog operationLog);

    /**
     * 多条件分页查询运行日志（列表视图，不含请求参数和异常堆栈）
     */
    IPage<OperationLogPageDto> pageOperationLogs(Integer page, Integer size,
                                                  String username, String module,
                                                  Integer logType, Integer resultStatus,
                                                  Integer systemType,
                                                  String startTime, String endTime);

    /**
     * 根据ID查询运行日志详情（含请求参数和异常堆栈等全字段）
     */
    OperationLogDetailVo getDetailById(Long id);

    /**
     * 日志统计总览：登录总次数/成功/失败、操作总次数、业务异常/系统异常次数
     */
    LogOverviewVo getOverview();

    /**
     * 运行日志趋势统计：最近7天每天的正常/业务异常/系统异常次数
     */
    List<OperationTrendVo> getOperationTrend();

    /**
     * 访问IP排行：最近7天访问次数最多的3个IP
     */
    List<IpRankVo> getIpRank();

    /**
     * 每小时活跃用户统计：最近24小时每小时去重用户数
     */
    List<HourlyActiveVo> getHourlyActiveUsers();
}
