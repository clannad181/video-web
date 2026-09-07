package com.cjym.videoweb.service;

import com.cjym.videoweb.model.vo.MessageVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 消息 WebSocket 推送服务
 * <p>
 * 维护 用户ID → 在线会话集合 的映射，消息落库后按接收者推送 JSON。
 * 同一用户可多端在线，逐个会话推送，推送失败仅移除会话并记录日志，不影响主流程。
 */
@Service
@Slf4j
public class MessagePushService {

    /** 用户ID → 在线 WebSocket 会话集合 */
    private final Map<Long, Set<WebSocketSession>> sessionMap = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 注册用户在线会话（握手成功后调用）
     */
    public void register(Long userId, WebSocketSession session) {
        sessionMap.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    /**
     * 移除用户会话（连接关闭/异常时调用）
     */
    public void remove(Long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionMap.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessionMap.remove(userId);
            }
        }
    }

    /**
     * 向指定用户推送消息（JSON 序列化后逐会话发送，失败仅记日志）
     *
     * @return 是否至少成功推送一个会话
     */
    public boolean push(Long userId, MessageVo messageVo) {
        Set<WebSocketSession> sessions = sessionMap.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }
        String json;
        try {
            json = objectMapper.writeValueAsString(messageVo);
        } catch (Exception e) {
            log.error("消息序列化失败[userId={}]", userId, e);
            return false;
        }
        boolean pushed = false;
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                    pushed = true;
                }
            } catch (IOException e) {
                log.warn("消息推送失败[userId={},sessionId={}]，移除会话", userId, session.getId(), e);
                remove(userId, session);
            }
        }
        return pushed;
    }
}
