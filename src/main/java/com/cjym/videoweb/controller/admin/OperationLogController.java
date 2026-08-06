package com.cjym.videoweb.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.OperationLogPageDto;
import com.cjym.videoweb.model.vo.IpRankVo;
import com.cjym.videoweb.model.vo.LogOverviewVo;
import com.cjym.videoweb.model.vo.LoginAccountRankVo;
import com.cjym.videoweb.model.vo.OperationLogDetailVo;
import com.cjym.videoweb.model.vo.OperationTrendVo;
import com.cjym.videoweb.model.vo.HourlyActiveVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.LoginLogService;
import com.cjym.videoweb.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 运行日志管理
 */
@RestController
@RequestMapping("/admin/operationLog")
@Tag(name = "运行日志管理", description = "运行日志管理")
public class OperationLogController {

    @Resource
    private OperationLogService operationLogService;

    @Resource
    private LoginLogService loginLogService;

    @GetMapping("/list")
    @Operation(summary = "多条件分页查询运行日志", description = "支持按用户名、模块、日志类型、执行状态、系统类型、时间范围筛选")
    public R<IPage<OperationLogPageDto>> listOperationLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Integer logType,
            @RequestParam(required = false) Integer resultStatus,
            @RequestParam(required = false) Integer systemType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return R.success(operationLogService.pageOperationLogs(page, size, username, module,
                logType, resultStatus, systemType, startTime, endTime));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询运行日志详情", description = "根据ID查询运行日志详情，含请求参数和异常堆栈等全字段")
    public R<OperationLogDetailVo> getOperationLogDetail(@PathVariable Long id) {
        return R.success(operationLogService.getDetailById(id));
    }

    @GetMapping("/overview")
    @Operation(summary = "日志统计总览", description = "统计登录总次数/成功/失败、操作总次数、业务异常/系统异常次数")
    public R<LogOverviewVo> getLogOverview() {
        return R.success(operationLogService.getOverview());
    }

    @GetMapping("/trend")
    @Operation(summary = "运行日志趋势统计", description = "统计最近7天每天的正常/业务异常/系统异常次数，无数据的日期填0")
    public R<List<OperationTrendVo>> getOperationTrend() {
        return R.success(operationLogService.getOperationTrend());
    }

    @GetMapping("/accountRank")
    @Operation(summary = "登录账号排行", description = "最近7天登录次数最多的3个账号")
    public R<List<LoginAccountRankVo>> getLoginAccountRank() {
        return R.success(loginLogService.getLoginAccountRank());
    }

    @GetMapping("/ipRank")
    @Operation(summary = "访问IP排行", description = "最近7天访问次数最多的3个IP地址")
    public R<List<IpRankVo>> getIpRank() {
        return R.success(operationLogService.getIpRank());
    }

    @GetMapping("/hourlyActive")
    @Operation(summary = "每小时活跃用户", description = "最近24小时每小时去重用户数，无数据的时段填0")
    public R<List<HourlyActiveVo>> getHourlyActiveUsers() {
        return R.success(operationLogService.getHourlyActiveUsers());
    }


}
