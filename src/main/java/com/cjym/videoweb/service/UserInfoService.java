package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.UserDto;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.model.vo.CaptchaVo;
import com.cjym.videoweb.model.vo.UserLoginVo;
import com.cjym.videoweb.result.R;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {

    /**
     * 生成验证码
     * @param captchaType 验证码类型
     * @param email 邮箱（非必填）
     * */
    R<CaptchaVo> getCaptcha(String captchaType, String email);

    /**
     * 用户登录
     * @param userLoginVo 登录信息
     * */
    R<Map<String,Object>> login(UserLoginVo userLoginVo);

    /**
     * 获取用户信息
     * */
    R<UserDto> getUserInfo(HttpServletRequest request);

    /**
     * token刷新
     * */
    R<Map<String, Object>> refresh(HttpServletRequest request);
}
