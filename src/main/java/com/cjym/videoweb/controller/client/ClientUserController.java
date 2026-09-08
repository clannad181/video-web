package com.cjym.videoweb.controller.client;

import com.cjym.videoweb.model.dto.ClientUserUpdateDto;
import com.cjym.videoweb.model.vo.CaptchaVo;
import com.cjym.videoweb.model.vo.ClientUserVo;
import com.cjym.videoweb.model.vo.UpUserVo;
import com.cjym.videoweb.model.vo.UserLoginVo;
import com.cjym.videoweb.model.vo.UserRegisterVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(summary = "获取验证码", description = "获取前台邮箱验证码（captchaType=email_captcha 登录验证码，captchaType=register_email_captcha 注册验证码）")
    public R<CaptchaVo> getCaptcha(@RequestParam String captchaType,
                                   @RequestParam(required = false) String email) {
        return userInfoService.getCaptcha(captchaType, email);
    }

    @PostMapping("/register")
    @Operation(summary = "前台用户注册", description = "前台用户注册（邮箱验证码校验，注册用户类型固定为前台普通用户，注册成功后自动登录并返回accessToken/refreshToken）")
    public R<Map<String, Object>> register(@RequestBody UserRegisterVo userRegisterVo) {
        return userInfoService.register(userRegisterVo);
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

    /**
     * 按uid获取指定UP主的公开信息（游客可访问，未登录时 isFollowed 为 false）
     *
     * @param userUid UP主uid（16位随机数字）
     */
    @GetMapping("/up-info")
    @Operation(summary = "获取指定UP主的公开信息", description = "按uid获取指定UP主的公开信息（uid/昵称/头像/签名/性别/大会员/关注数/粉丝数/累计获赞/累计播放/当前用户是否已关注），游客可访问（未登录时 isFollowed 为 false），UP主不存在时报错")
    public R<UpUserVo> getUpUserInfo(
            @Parameter(description = "UP主uid（16位随机数字）") @RequestParam String userUid) {
        return R.success(userInfoService.getUpUserInfo(userUid));
    }

    @PostMapping("/update")
    @Operation(summary = "更新前台用户个人信息", description = "更新当前登录前台用户的个人信息（昵称、头像、性别、生日、个性签名），至少传一个字段，仅更新传入的非空字段")
    public R<String> updateUserInfo(@RequestBody ClientUserUpdateDto dto, HttpServletRequest request) {
        return userInfoService.updateClientUserInfo(request, dto);
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
