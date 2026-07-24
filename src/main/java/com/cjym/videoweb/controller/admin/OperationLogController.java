package com.cjym.videoweb.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.OperationLogPageDto;
import com.cjym.videoweb.model.vo.LogOverviewVo;
import com.cjym.videoweb.model.vo.OperationLogDetailVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 运行日志管理
 */
@RestController
@RequestMapping("/admin/operationLog")
@Tag(name = "运行日志管理", description = "运行日志管理")
public class OperationLogController {

    @Resource
    private OperationLogService operationLogService;

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


}
