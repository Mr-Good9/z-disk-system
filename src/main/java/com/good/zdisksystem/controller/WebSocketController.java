package com.good.zdisksystem.controller;

import com.good.zdisksystem.model.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    
    @MessageMapping("/user.status")
    @SendTo("/topic/status.update")
    public UserStatus handleStatus(UserStatus status) {
        logger.info("收到状态更新: {}", status);
        try {
            // 可以在这里添加额外的状态处理逻辑
            if (status.getUserId() == null || status.getStatus() == null) {
                logger.warn("收到无效的状态更新: {}", status);
                return null;
            }
            return status;
        } catch (Exception e) {
            logger.error("处理状态更新时出错", e);
            return null;
        }
    }
} 