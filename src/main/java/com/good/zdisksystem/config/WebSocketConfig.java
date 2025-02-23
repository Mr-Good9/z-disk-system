package com.good.zdisksystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * 用于配置WebSocket连接端点和消息代理
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 配置消息代理
        // /queue 用于点对点消息推送
        // /topic 用于广播消息推送
        registry.enableSimpleBroker("/queue", "/topic");
        // 配置客户端发送消息的前缀
        registry.setApplicationDestinationPrefixes("/app");
        // 配置点对点消息的前缀
        registry.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 配置WebSocket连接端点
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // 允许所有来源的连接
                .withSockJS();  // 启用SockJS支持
    }
} 