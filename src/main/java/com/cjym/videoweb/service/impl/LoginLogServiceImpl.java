package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.mapper.LoginLogMapper;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.model.dto.LoginLogPageDto;
import com.cjym.videoweb.model.entity.LoginLog;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.model.vo.LoginAccountRankVo;
import com.cjym.videoweb.model.vo.LoginTrendVo;
import com.cjym.videoweb.service.LoginLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 登录日志 Service 实现
 */
@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements LoginLogService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public void recordLoginSuccess(LoginLog loginLog) {
        // 根据账号反查 userId
        fillUserId(loginLog);
        loginLog.setLoginStatus(1);
        loginLog.setLoginTime(LocalDateTime.now());
        baseMapper.insert(loginLog);
    }

    @Override
    public void recordLoginFailure(LoginLog loginLog) {
        // 根据账号反查 userId
        fillUserId(loginLog);
        loginLog.setLoginStatus(0);
        loginLog.setLoginTime(LocalDateTime.now());
        baseMapper.insert(loginLog);
    }

    @Override
    public IPage<LoginLogPageDto> pageLoginLogs(Integer page, Integer size,
                                                 String account, Integer systemType,
                                                 Integer loginStatus, String loginType,
                                                 String startTime, String endTime) {
        Page<LoginLogPageDto> pageParam = new Page<>(page, size);
        return baseMapper.selectLoginLogPage(pageParam, account, systemType,
                loginStatus, loginType, startTime, endTime);
    }

    @Override
    public List<LoginTrendVo> getLoginTrend() {
        List<LoginTrendVo> dbList = baseMapper.selectLoginTrend();
        // 转为 Map 以快速查找
        Map<LocalDate, LoginTrendVo> dateMap = dbList.stream()
                .collect(Collectors.toMap(LoginTrendVo::getDate, v -> v));
        // 补全最近7天（含今天），无数据的日期填 0
        LocalDate today = LocalDate.now();
        List<LoginTrendVo> result = new ArrayList<>(7);
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LoginTrendVo vo = dateMap.get(day);
            if (vo == null) {
                vo = new LoginTrendVo();
                vo.setDate(day);
                vo.setLoginSuccess(0L);
                vo.setLoginFailure(0L);
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<LoginAccountRankVo> getLoginAccountRank() {
        return baseMapper.selectLoginAccountRank();
    }

    /**
     * 根据 account（用户名或邮箱）反查 userId，找不到则为 null
     */
    private void fillUserId(LoginLog loginLog) {
        if (loginLog.getAccount() == null) {
            return;
        }
        try {
            UserInfo userInfo = userInfoMapper.selectByUserName(loginLog.getAccount());
            if (userInfo == null) {
                // 尝试按邮箱查询
                userInfo = userInfoMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserInfo>()
                                .eq(UserInfo::getEmail, loginLog.getAccount())
                );
            }
            if (userInfo != null) {
                loginLog.setUserId(userInfo.getId());
            }
        } catch (Exception ignored) {
            // 查不到用户则 userId 为空
        }
    }
}
