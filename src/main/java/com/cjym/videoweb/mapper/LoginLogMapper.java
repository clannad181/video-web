package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.dto.LoginLogPageDto;
import com.cjym.videoweb.model.entity.LoginLog;
import com.cjym.videoweb.model.vo.LoginAccountRankVo;
import com.cjym.videoweb.model.vo.LoginTrendVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 登录日志 Mapper
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {

    /**
     * 多条件分页查询登录日志
     */
    IPage<LoginLogPageDto> selectLoginLogPage(Page<LoginLogPageDto> page,
                                              @Param("account") String account,
                                              @Param("systemType") Integer systemType,
                                              @Param("loginStatus") Integer loginStatus,
                                              @Param("loginType") String loginType,
                                              @Param("startTime") String startTime,
                                              @Param("endTime") String endTime);

    /**
     * 登录趋势统计：最近7天每天的成功/失败次数
     */
    List<LoginTrendVo> selectLoginTrend();

    /**
     * 登录账号排行：最近7天登录次数最多的3个账号
     */
    List<LoginAccountRankVo> selectLoginAccountRank();

    /**
     * 今日登录人数
     */
    Long selectTodayLoginCount();
}
