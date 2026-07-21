package com.cjym.videoweb.controller.admin;


import com.cjym.videoweb.model.dto.UserDto;
import com.cjym.videoweb.model.vo.CaptchaVo;
import com.cjym.videoweb.model.vo.UserLoginVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/user")
@Tag(name = "用户信息管理", description = "用户信息管理")
public class UserInfoController {
    @Resource
    private UserInfoService userInfoService;


    @GetMapping("/captcha")
    @Operation(summary = "获取验证码", description = "获取验证码")
    public R<CaptchaVo> getCaptcha(@RequestParam String captchaType, @RequestParam(required = false) String email) {
        return userInfoService.getCaptcha(captchaType, email);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录")
    public R<Map<String,Object>> login(@RequestBody UserLoginVo userLoginVo) {
        return userInfoService.login(userLoginVo);
    }

    @GetMapping("/userInfo")
    @Operation(summary = "获取用户信息",description = "获取用户信息")
    public R<UserDto> getUserInfo(HttpServletRequest request) {
        return userInfoService.getUserInfo(request);
    }

    @GetMapping("/refresh")
    @Operation(summary = "token刷新",description = "token刷新")
    public R<Map<String,Object>> refresh(HttpServletRequest request) {
        return userInfoService.refresh(request);
    }
}
