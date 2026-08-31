package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.OperationLogMapper;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.model.dto.OperationLogPageDto;
import com.cjym.videoweb.model.entity.OperationLog;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.model.vo.IpRankVo;
import com.cjym.videoweb.model.vo.LogOverviewVo;
import com.cjym.videoweb.model.vo.OperationLogDetailVo;
import com.cjym.videoweb.model.vo.OperationTrendVo;
import com.cjym.videoweb.model.vo.HourlyActiveVo;
import com.cjym.videoweb.service.OperationLogService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 运行日志 Service 实现
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public void saveLog(OperationLog operationLog) {
        operationLog.setOperationTime(LocalDateTime.now());
        baseMapper.insert(operationLog);
    }

    @Override
    public IPage<OperationLogPageDto> pageOperationLogs(Integer page, Integer size,
                                                         String username, String module,
                                                         Integer logType, Integer resultStatus,
                                                         Integer systemType,
                                                         String startTime, String endTime) {
        Page<OperationLogPageDto> pageParam = new Page<>(page, size);
        return baseMapper.selectOperationLogPage(pageParam, username, module,
                logType, resultStatus, systemType, startTime, endTime);
    }

    @Override
    public OperationLogDetailVo getDetailById(Long id) {
        if (id == null) {
            throw new GlobeException("日志ID不能为空");
        }
        OperationLog operationLog = baseMapper.selectById(id);
        if (operationLog == null) {
            throw new GlobeException("日志不存在");
        }
        OperationLogDetailVo vo = new OperationLogDetailVo();
        BeanUtils.copyProperties(operationLog, vo);
        // 新日志仅记录 userId，按 userId 反查用户名展示
        if (operationLog.getUserId() != null) {
            UserInfo userInfo = userInfoMapper.selectById(operationLog.getUserId());
            if (userInfo != null) {
                vo.setUsername(userInfo.getUserName());
            }
        }
        return vo;
    }

    @Override
    public LogOverviewVo getOverview() {
        return baseMapper.selectLogOverview();
    }

    @Override
    public List<OperationTrendVo> getOperationTrend() {
        List<OperationTrendVo> dbList = baseMapper.selectOperationTrend();
        Map<LocalDate, OperationTrendVo> dateMap = dbList.stream()
                .collect(Collectors.toMap(OperationTrendVo::getDate, v -> v));
        // 补全最近7天（含今天），无数据的日期填 0
        LocalDate today = LocalDate.now();
        List<OperationTrendVo> result = new ArrayList<>(7);
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            OperationTrendVo vo = dateMap.get(day);
            if (vo == null) {
                vo = new OperationTrendVo();
                vo.setDate(day);
                vo.setNormalCount(0L);
                vo.setBusinessException(0L);
                vo.setSystemException(0L);
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<IpRankVo> getIpRank() {
        return baseMapper.selectIpRank();
    }

    @Override
    public List<HourlyActiveVo> getHourlyActiveUsers() {
        List<HourlyActiveVo> dbList = baseMapper.selectHourlyActiveUsers();
        Map<String, HourlyActiveVo> hourMap = dbList.stream()
                .collect(Collectors.toMap(HourlyActiveVo::getHour, v -> v));
        // 补全最近24小时，无数据的时段填 0
        List<HourlyActiveVo> result = new ArrayList<>(24);
        LocalDateTime now = LocalDateTime.now();
        for (int i = 23; i >= 0; i--) {
            LocalDateTime dt = now.minusHours(i).withMinute(0).withSecond(0).withNano(0);
            String hourKey = String.format("%02d:00", dt.getHour());
            HourlyActiveVo vo = hourMap.get(hourKey);
            if (vo == null) {
                vo = new HourlyActiveVo(hourKey, 0L);
            }
            result.add(vo);
        }
        return result;
    }
}
