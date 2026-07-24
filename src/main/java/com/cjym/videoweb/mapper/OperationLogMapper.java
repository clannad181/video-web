package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.dto.OperationLogPageDto;
import com.cjym.videoweb.model.entity.OperationLog;
import com.cjym.videoweb.model.vo.LogOverviewVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}
