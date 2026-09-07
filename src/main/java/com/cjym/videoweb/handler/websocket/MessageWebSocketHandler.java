package com.cjym.videoweb.handler.websocket;

import com.cjym.videoweb.service.MessagePushService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 消息 WebSocket 处理器：连接建立时注册会话，断开/异常时清理会话
 */
@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {

    @Resource
    private MessagePushService messagePushService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = (Long) session.getAttributes().get(MessageHandshakeInterceptor.ATTRIBUTE_USER_ID);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        messagePushService.register(userId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get(MessageHandshakeInterceptor.ATTRIBUTE_USER_ID);
        if (userId != null) {
            messagePushService.remove(userId, session);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = (Long) session.getAttributes().get(MessageHandshakeInterceptor.ATTRIBUTE_USER_ID);
        if (userId != null) {
            messagePushService.remove(userId, session);
        }
        super.handleTransportError(session, exception);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 客户端心跳等消息暂不处理
    }
}
