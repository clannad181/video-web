package com.cjym.videoweb.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.UserPageDto;
import com.cjym.videoweb.model.vo.CaptchaVo;
import com.cjym.videoweb.model.vo.PasswordVo;
import com.cjym.videoweb.model.vo.UserLoginVo;
import com.cjym.videoweb.model.vo.UserStatsVo;
import com.cjym.videoweb.model.vo.UserAddVo;
import com.cjym.videoweb.model.vo.UserUpdateVo;
import com.cjym.videoweb.model.vo.UserVo;
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
    public R<UserVo> getUserInfo(HttpServletRequest request) {
        return userInfoService.getUserInfo(request);
    }

    @GetMapping("/refresh")
    @Operation(summary = "token刷新",description = "token刷新")
    public R<Map<String,Object>> refresh(HttpServletRequest request) {
        return userInfoService.refresh(request);
    }

    @GetMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出，使refresh-token失效")
    public R<String> logout(HttpServletRequest request) {
        return userInfoService.logout(request);
    }

    @PostMapping("/resetPassword")
    @Operation(summary = "重置密码", description = "重置密码")
    public R<String> resetPassword(@RequestBody PasswordVo passwordVo) {
        return userInfoService.resetPassword(passwordVo);
    }

    @PostMapping("/add")
    @Operation(summary = "新增用户", description = "新增用户")
    public R<String> addUser(@RequestBody UserAddVo userAddVo) {
        return userInfoService.addUser(userAddVo);
    }

    @PostMapping("/update")
    @Operation(summary = "更新用户", description = "更新用户信息（只更新传入的非空字段）")
    public R<String> updateUser(@RequestBody UserUpdateVo userUpdateVo) {
        return userInfoService.updateUser(userUpdateVo);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除用户", description = "删除用户（逻辑删除）")
    public R<String> deleteUser(@PathVariable Long id) {
        return userInfoService.deleteUser(id);
    }

    @GetMapping("/list")
    @Operation(summary = "多条件分页查询用户列表", description = "多条件分页查询用户列表（用户名、昵称、邮箱、状态、角色、类型、大会员）")
    public R<IPage<UserPageDto>> listUsers(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) String userName,
                                           @RequestParam(required = false) String nikeName,
                                           @RequestParam(required = false) String email,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) Long roleId,
                                           @RequestParam(required = false) Integer type,
                                           @RequestParam(required = false) Boolean isVip) {
        return userInfoService.listUsers(page, size, userName, nikeName, email, status, roleId, type, isVip);
    }

    @GetMapping("/stats")
    @Operation(summary = "用户统计总览", description = "总数/大会员/管理员 + 今日/昨日新增 + 今日/昨日活跃，含同比变化")
    public R<UserStatsVo> getUserStats() {
        return R.success(userInfoService.getUserStats());
    }

}
