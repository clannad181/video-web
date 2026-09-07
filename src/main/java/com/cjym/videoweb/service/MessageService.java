package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.entity.MessageInfo;
import com.cjym.videoweb.model.vo.MessageVo;

import java.util.List;

public interface MessageService {

    /**
     * 创建单条消息（补充 avid 与未读状态后落库并实时推送 WebSocket）
     */
    void create(MessageInfo message);

    /**
     * 批量创建消息（同一内容发送给多个接收者，如服务器告警广播给所有管理员）
     */
    void createBatch(List<Long> userIds, Integer type, String title, String content, Long bizId, String bizAvid);

    /**
     * 分页查询当前登录用户的消息，支持按类型与已读状态筛选，按创建时间倒序
     */
    IPage<MessageVo> pageMyMessages(Integer page, Integer size, Integer type, Integer isRead);

    /**
     * 当前登录用户的未读消息数
     */
    Long unreadCount();

    /**
     * 按消息avid标记单条消息已读（仅接收者可操作）
     */
    void markRead(String messageUid);

    /**
     * 将当前登录用户的全部未读消息标记为已读
     */
    void markAllRead();
}
