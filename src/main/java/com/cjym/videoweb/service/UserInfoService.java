package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.model.vo.CaptchaVo;
import com.cjym.videoweb.model.vo.UserLoginVo;
import com.cjym.videoweb.result.R;

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
    void login(UserLoginVo userLoginVo);
}
