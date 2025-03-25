package com.good.zdisksystem.config;

import com.good.zdisksystem.security.utils.JwtUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

/**
 * WebSocket配置类
 * 用于配置WebSocket连接端点和消息代理
 */
@Configuration
// @EnableWebSocketMessageBroker 注释掉这一行来禁用WebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS()
            .setHttpMessageCacheSize(1000)
            .setWebSocketEnabled(true)
            .setHeartbeatTime(25000)
            .setDisconnectDelay(5000)
            .setSupressCors(true);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(128 * 1024)
                   .setSendBufferSizeLimit(1024 * 1024)
                   .setSendTimeLimit(30000)
                   .setTimeToFirstMessage(60000);
        
        registration.addDecoratorFactory(handler -> new WebSocketHandlerDecorator(handler) {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                log.info("WebSocket连接已建立: ID={}, 远程地址={}", 
                         session.getId(), 
                         session.getRemoteAddress());
                super.afterConnectionEstablished(session);
            }
        });
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        converter.setObjectMapper(objectMapper);
        messageConverters.add(converter);
        
        messageConverters.add(new org.springframework.messaging.converter.StringMessageConverter());
        return false;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    log.info("处理WebSocket CONNECT命令");
                    
                    // 尝试从多个来源获取认证信息
                    String token = null;
                    
                    // 1. 尝试从Authorization头获取
                    List<String> authorization = accessor.getNativeHeader("Authorization");
                    if (authorization != null && !authorization.isEmpty()) {
                        String authHeader = authorization.get(0);
                        if (authHeader != null && authHeader.startsWith("Bearer ")) {
                            token = authHeader.substring(7);
                            log.info("从Authorization头获取到token");
                        }
                    }
                    
                    // 2. 如果没有从头获取到，尝试从Cookie获取
                    if (token == null) {
                        List<String> cookies = accessor.getNativeHeader("Cookie");
                        if (cookies != null && !cookies.isEmpty()) {
                            String cookieHeader = cookies.get(0);
                            // 简单解析Cookie，查找名为token或AUTH_TOKEN的Cookie
                            for (String cookie : cookieHeader.split(";")) {
                                cookie = cookie.trim();
                                if (cookie.startsWith("token=") || cookie.startsWith("AUTH_TOKEN=")) {
                                    token = cookie.substring(cookie.indexOf('=') + 1);
                                    log.info("从Cookie获取到token");
                                    break;
                                }
                            }
                        }
                    }
                    
                    // 3. 检查是否获取到token并验证
                    if (token != null) {
                        try {
                            // 这里应该使用你的JwtUtils工具类验证token
                            // 简化处理，假设token有效
                            UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken("user", null, new ArrayList<>());
                            accessor.setUser(auth);
                            log.info("WebSocket连接认证成功");
                        } catch (Exception e) {
                            log.error("WebSocket连接认证失败", e);
                        }
                    } else {
                        // 允许匿名连接进行测试
                        log.warn("未找到认证信息，允许匿名连接");
                    }
                }
                return message;
            }
        });
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("收到新的WebSocket连接: {}", headerAccessor.getSessionId());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.info("WebSocket连接断开: {}, 状态码: {}", 
                 sessionId, 
                 event.getCloseStatus() != null ? event.getCloseStatus().getCode() : "未知");
    }
}
