package com.cjym.videoweb.controller.client;

import com.cjym.videoweb.model.vo.CaptchaVo;
import com.cjym.videoweb.model.vo.ClientUserVo;
import com.cjym.videoweb.model.vo.UserLoginVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户登录与信息（客户端）
 */
@RestController
@RequestMapping("/client/user")
@Tag(name = "用户登录与信息", description = "前台用户登录、获取用户基础信息")
public class ClientUserController {

    @Resource
    private UserInfoService userInfoService;

    @GetMapping("/captcha")
    @Operation(summary = "获取验证码", description = "获取前台登录验证码（邮箱验证码）")
    public R<CaptchaVo> getCaptcha(@RequestParam String captchaType,
                                   @RequestParam(required = false) String email) {
        return userInfoService.getCaptcha(captchaType, email);
    }

    @PostMapping("/login")
    @Operation(summary = "前台用户登录", description = "前台用户通过邮箱验证码登录，loginSystem 自动设为 0（前台）")
    public R<Map<String, Object>> login(@RequestBody UserLoginVo userLoginVo) {
        // 确保前台登录标识为 0
        userLoginVo.setLoginSystem(1);
        return userInfoService.login(userLoginVo);
    }

    @GetMapping("/userInfo")
    @Operation(summary = "获取前台用户基础信息", description = "获取当前登录前台用户的基础信息（不含角色和菜单）")
    public R<ClientUserVo> getUserInfo(HttpServletRequest request) {
        return userInfoService.getClientUserInfo(request);
    }

    @GetMapping("/refresh")
    @Operation(summary = "token刷新", description = "刷新前台用户的 access token")
    public R<Map<String, Object>> refresh(HttpServletRequest request) {
        return userInfoService.refresh(request);
    }

    @GetMapping("/logout")
    @Operation(summary = "用户登出", description = "前台用户登出，使 refresh-token 失效")
    public R<String> logout(HttpServletRequest request) {
        return userInfoService.logout(request);
    }
}
