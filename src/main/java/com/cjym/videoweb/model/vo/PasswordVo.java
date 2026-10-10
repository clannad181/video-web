package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PasswordVo {
    @Schema(description = "用户Id")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "旧密码")
    private String oldPassword;

    @Schema(description = "新密码")
    private String newPassword;

    @Schema(description = "验证码Key")
    private String key;

    @Schema(description = "验证码值")
    private String captcha;

    @Schema(description = "用户类型")
    private Integer type;
}
