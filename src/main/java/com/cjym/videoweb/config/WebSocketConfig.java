package com.cjym.videoweb.config;

import com.cjym.videoweb.handler.websocket.MessageHandshakeInterceptor;
import com.cjym.videoweb.handler.websocket.MessageWebSocketHandler;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置：注册消息推送端点
 * <p>
 * 连接地址：ws://host:8899/api/ws/message?token={access_token}（context-path 为 /api）
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private MessageWebSocketHandler messageWebSocketHandler;

    @Resource
    private MessageHandshakeInterceptor messageHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageWebSocketHandler, "/ws/message")
                .addInterceptors(messageHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
