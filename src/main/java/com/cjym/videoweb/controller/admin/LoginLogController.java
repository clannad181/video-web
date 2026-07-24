package com.cjym.videoweb.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.LoginLogPageDto;
import com.cjym.videoweb.model.vo.LoginTrendVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录日志管理
 */
@RestController
@RequestMapping("/admin/loginLog")
@Tag(name = "登录日志管理", description = "登录日志管理")
public class LoginLogController {

    @Resource
    private LoginLogService loginLogService;

    @GetMapping("/list")
    @Operation(summary = "多条件分页查询登录日志", description = "支持按账号、系统类型、登录状态、登录方式、时间范围筛选")
    public R<IPage<LoginLogPageDto>> listLoginLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String account,
            @RequestParam(required = false) Integer systemType,
            @RequestParam(required = false) Integer loginStatus,
            @RequestParam(required = false) String loginType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return R.success(loginLogService.pageLoginLogs(page, size, account,
                systemType, loginStatus, loginType, startTime, endTime));
    }

    @GetMapping("/trend")
    @Operation(summary = "登录趋势统计", description = "统计最近7天每天的登录成功/失败次数，无数据的日期填0")
    public R<List<LoginTrendVo>> getLoginTrend() {
        return R.success(loginLogService.getLoginTrend());
    }
}
