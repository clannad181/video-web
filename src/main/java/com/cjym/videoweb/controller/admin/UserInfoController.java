package com.cjym.videoweb.controller.admin;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/user")
@Tag(name = "用户信息管理", description = "用户信息管理")
public class UserInfoController {

    @GetMapping("/captcha")
    @Operation(summary = "获取验证码", description = "获取验证码")
    public String getCaptcha(String captchaType,String username) {

        return "captcha";
    }
}
