package com.cjym.videoweb.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "运行日志分页列表")
public class OperationLogPageDto {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "操作用户名")
    private String username;

    @Schema(description = "所属模块")
    private String module;

    @Schema(description = "操作描述")
    private String operation;

    @Schema(description = "执行方法（类名.方法名）")
    private String method;

    @Schema(description = "请求URI")
    private String requestUri;

    @Schema(description = "请求方式：GET/POST/PUT/DELETE")
    private String requestMethod;

    @Schema(description = "请求IP地址")
    private String ipAddress;

    @Schema(description = "执行耗时（毫秒）")
    private Long executionTime;

    @Schema(description = "执行状态：0-系统异常，1-业务异常，2-成功")
    private Integer resultStatus;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "日志类型：0-系统异常，1-业务操作")
    private Integer logType;

    @Schema(description = "系统类型：1-后台管理，0-前台用户")
    private Integer systemType;

    @Schema(description = "操作时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;
}
