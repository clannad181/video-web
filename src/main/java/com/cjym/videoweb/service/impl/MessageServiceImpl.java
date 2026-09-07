package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.constant.MessageConstant;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.MessageMapper;
import com.cjym.videoweb.model.entity.MessageInfo;
import com.cjym.videoweb.model.vo.MessageVo;
import com.cjym.videoweb.service.MessagePushService;
import com.cjym.videoweb.service.MessageService;
import com.cjym.videoweb.utils.Base62Converter;
import com.cjym.videoweb.utils.JWTUtil;
import com.cjym.videoweb.utils.SnowflakeIdGenerator;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MessageServiceImpl extends ServiceImpl<MessageMapper, MessageInfo> implements MessageService {

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Resource
    private MessagePushService messagePushService;

    @Override
    public void create(MessageInfo message) {
        if (message == null || message.getUserId() == null) {
            return;
        }
        // 补充 avid 与未读状态后落库
        message.setAvid(Base62Converter.toAvid(snowflakeIdGenerator.nextId()));
        message.setIsRead(MessageConstant.READ_UNREAD);
        save(message);
        pushToUser(message);
    }

    @Override
    public void createBatch(List<Long> userIds, Integer type, String title, String content, Long bizId, String bizAvid) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            MessageInfo message = new MessageInfo();
            message.setUserId(userId);
            message.setType(type);
            message.setTitle(title);
            message.setContent(content);
            message.setBizId(bizId);
            message.setBizAvid(bizAvid);
            create(message);
        }
    }

    @Override
    public IPage<MessageVo> pageMyMessages(Integer page, Integer size, Integer type, Integer isRead) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        int pageNum = page != null ? page : 1;
        int pageSize = size != null ? size : 10;
        LambdaQueryWrapper<MessageInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageInfo::getUserId, currentUserId);
        if (type != null) {
            wrapper.eq(MessageInfo::getType, type);
        }
        if (isRead != null) {
            wrapper.eq(MessageInfo::getIsRead, isRead);
        }
        wrapper.orderByDesc(MessageInfo::getCreateTime);
        IPage<MessageInfo> pageResult = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        IPage<MessageVo> result = new Page<>(pageNum, pageSize, pageResult.getTotal());
        result.setRecords(pageResult.getRecords().stream().map(this::toVo).toList());
        return result;
    }

    @Override
    public Long unreadCount() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        LambdaQueryWrapper<MessageInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageInfo::getUserId, currentUserId)
                .eq(MessageInfo::getIsRead, MessageConstant.READ_UNREAD);
        return baseMapper.selectCount(wrapper);
    }

    @Override
    public void markRead(String messageUid) {
        if (StringUtils.isEmpty(messageUid)) {
            throw new GlobeException("消息avid不能为空");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        LambdaQueryWrapper<MessageInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageInfo::getAvid, messageUid)
                .eq(MessageInfo::getUserId, currentUserId);
        MessageInfo message = baseMapper.selectOne(wrapper);
        if (message == null) {
            throw new GlobeException("消息不存在");
        }
        if (MessageConstant.READ_READ == message.getIsRead()) {
            return;
        }
        message.setIsRead(MessageConstant.READ_READ);
        message.setReadTime(LocalDateTime.now());
        baseMapper.updateById(message);
    }

    @Override
    public void markAllRead() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        baseMapper.update(null, new LambdaUpdateWrapper<MessageInfo>()
                .eq(MessageInfo::getUserId, currentUserId)
                .eq(MessageInfo::getIsRead, MessageConstant.READ_UNREAD)
                .set(MessageInfo::getIsRead, MessageConstant.READ_READ)
                .set(MessageInfo::getReadTime, LocalDateTime.now()));
    }

    /**
     * 实体转 VO 后推送 WebSocket（离线用户仅落库，上线后通过列表接口拉取）
     */
    private void pushToUser(MessageInfo message) {
        MessageVo vo = toVo(message);
        try {
            messagePushService.push(message.getUserId(), vo);
        } catch (Exception e) {
            log.warn("消息实时推送失败[userId={}]，消息已落库可接口拉取", message.getUserId(), e);
        }
    }

    private MessageVo toVo(MessageInfo message) {
        MessageVo vo = new MessageVo();
        BeanUtils.copyProperties(message, vo);
        return vo;
    }

    /**
     * 从请求 JWT token 中获取当前用户ID
     */
    private Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader("authorization");
            if (StringUtils.isEmpty(token)) {
                return null;
            }
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            return JWTUtil.getUserId(token);
        } catch (Exception e) {
            return null;
        }
    }
}
