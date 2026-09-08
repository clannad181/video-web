package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "前台用户注册请求")
public class UserRegisterVo {

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "验证码Key")
    private String key;

    @Schema(description = "验证码")
    private String captcha;

    @Schema(description = "用户昵称（不传时默认使用用户名）")
    private String nikeName;
}
