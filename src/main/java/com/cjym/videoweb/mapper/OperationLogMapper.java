package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.dto.OperationLogPageDto;
import com.cjym.videoweb.model.entity.OperationLog;
import com.cjym.videoweb.model.vo.IpRankVo;
import com.cjym.videoweb.model.vo.LogOverviewVo;
import com.cjym.videoweb.model.vo.OperationTrendVo;
import com.cjym.videoweb.model.vo.HourlyActiveVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 运行日志 Mapper
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    /**
     * 多条件分页查询运行日志
     */
    IPage<OperationLogPageDto> selectOperationLogPage(Page<OperationLogPageDto> page,
                                                       @Param("username") String username,
                                                       @Param("module") String module,
                                                       @Param("logType") Integer logType,
                                                       @Param("resultStatus") Integer resultStatus,
                                                       @Param("systemType") Integer systemType,
                                                       @Param("startTime") String startTime,
                                                       @Param("endTime") String endTime);

    /**
     * 日志统计总览：同时统计 login_log 和 operation_log
     */
    LogOverviewVo selectLogOverview();

    /**
     * 运行日志趋势统计：最近7天每天的正常/业务异常/系统异常次数
     */
    List<OperationTrendVo> selectOperationTrend();

    /**
     * 访问IP排行：最近7天访问次数最多的3个IP
     */
    List<IpRankVo> selectIpRank();

    /**
     * 每小时活跃用户统计：最近24小时每小时去重用户数
     */
    List<HourlyActiveVo> selectHourlyActiveUsers();
}
