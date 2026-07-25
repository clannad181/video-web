package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.LoginLogPageDto;
import com.cjym.videoweb.model.entity.LoginLog;
import com.cjym.videoweb.model.vo.LoginAccountRankVo;
import com.cjym.videoweb.model.vo.LoginTrendVo;

import java.util.List;

/**
 * 登录日志 Service
 */
public interface LoginLogService extends IService<LoginLog> {

    /**
     * 记录登录成功日志
     */
    void recordLoginSuccess(LoginLog loginLog);

    /**
     * 记录登录失败日志
     */
    void recordLoginFailure(LoginLog loginLog);

    /**
     * 多条件分页查询登录日志
     */
    IPage<LoginLogPageDto> pageLoginLogs(Integer page, Integer size,
                                         String account, Integer systemType,
                                         Integer loginStatus, String loginType,
                                         String startTime, String endTime);

    /**
     * 登录趋势统计：最近7天每天的成功/失败次数
     */
    List<LoginTrendVo> getLoginTrend();

    /**
     * 登录账号排行：最近7天登录次数最多的3个账号
     */
    List<LoginAccountRankVo> getLoginAccountRank();
}
