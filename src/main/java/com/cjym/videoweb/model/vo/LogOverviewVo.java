package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "日志统计总览")
public class LogOverviewVo {

    @Schema(description = "登录总次数")
    private Long loginTotal;

    @Schema(description = "登录成功次数")
    private Long loginSuccess;

    @Schema(description = "登录失败次数")
    private Long loginFailure;

    @Schema(description = "操作总次数")
    private Long operationTotal;

    @Schema(description = "业务异常次数")
    private Long businessException;

    @Schema(description = "系统异常次数")
    private Long systemException;
}
