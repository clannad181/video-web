package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginVo {
    @Schema(name = "用户名")
    private String username;

    @Schema(name = "密码")
    private String password;

    @Schema(name = "邮箱")
    private String email;

    @Schema(name = "验证码Key")
    private String key;

    @Schema(name = "验证码")
    private String captcha;

    @Schema(name = "登录方式")
    private String loginType;

    @Schema(name = "登录系统")
    private Integer loginSystem;
}
