package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.UserPageDto;
import com.cjym.videoweb.model.vo.UserAddVo;
import com.cjym.videoweb.model.vo.UserUpdateVo;
import com.cjym.videoweb.model.vo.UserVo;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.model.vo.CaptchaVo;
import com.cjym.videoweb.model.vo.PasswordVo;
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
    R<UserVo> getUserInfo(HttpServletRequest request);

    /**
     * token刷新
     * */
    R<Map<String, Object>> refresh(HttpServletRequest request);

    /**
     * 重置密码
     * @param passwordVo 密码信息
     * */
    R<String> resetPassword(PasswordVo passwordVo);

    /**
     * 新增用户
     * @param userAddVo 新增用户请求
     * */
    R<String> addUser(UserAddVo userAddVo);

    /**
     * 更新用户
     * @param userUpdateVo 更新用户请求
     * */
    R<String> updateUser(UserUpdateVo userUpdateVo);

    /**
     * 删除用户（逻辑删除）
     * @param id 用户ID
     * */
    R<String> deleteUser(Long id);

    /**
     * 多条件分页查询用户列表（含角色名称）
     */
    R<IPage<UserPageDto>> listUsers(Integer page, Integer size,
                                    String userName, String nikeName, String email,
                                    Integer status, Long roleId, Integer type, Boolean isVip);
}
