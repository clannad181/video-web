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
import com.cjym.videoweb.model.vo.LogOverviewVo;
import com.cjym.videoweb.model.vo.OperationLogDetailVo;
import com.cjym.videoweb.service.OperationLogService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 运行日志 Service 实现
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public void saveLog(OperationLog operationLog) {
        // 根据用户名反查 userId
        fillUserId(operationLog);
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
        return vo;
    }

    @Override
    public LogOverviewVo getOverview() {
        return baseMapper.selectLogOverview();
    }

    /**
     * 根据用户名反查 userId
     */
    private void fillUserId(OperationLog operationLog) {
        if (operationLog.getUsername() == null) {
            return;
        }
        try {
            UserInfo userInfo = userInfoMapper.selectByUserName(operationLog.getUsername());
            if (userInfo != null) {
                operationLog.setUserId(userInfo.getId());
            }
        } catch (Exception ignored) {
            // 查不到用户则 userId 为空
        }
    }
}
