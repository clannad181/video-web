package com.cjym.videoweb.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "登录日志分页列表")
public class LoginLogPageDto {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "登录账号（用户名或邮箱）")
    private String account;

    @Schema(description = "登录方式：img_captcha-图片验证码登录，email_captcha-邮箱验证码登录")
    private String loginType;

    @Schema(description = "系统类型：1-后台管理，0-前台用户")
    private Integer systemType;

    @Schema(description = "登录IP地址")
    private String ipAddress;

    @Schema(description = "浏览器UserAgent")
    private String userAgent;

    @Schema(description = "登录状态：0-失败，1-成功")
    private Integer loginStatus;

    @Schema(description = "失败原因（登录成功时为空）")
    private String failReason;

    @Schema(description = "登录时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
}
