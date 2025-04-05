package com.good.zdisksystem.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

@Controller
public class WebSocketTestController {
    
    private static final Logger log = LoggerFactory.getLogger(WebSocketTestController.class);
    
    @GetMapping("/ws/info")
    public ResponseEntity<Map<String, Object>> getWebSocketInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("websocket", true);
        info.put("cookie_needed", false);
        info.put("origins", Arrays.asList("*"));
        info.put("entropy", Math.round(Math.random() * 9999999));
        log.info("SockJS info请求: /ws/info");
        return ResponseEntity.ok(info);
    }

    @MessageMapping("/test")
    @SendTo("/topic/test")
    public Map<String, Object> test(String message) {
        log.info("收到WebSocket测试消息: {}", message);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "收到消息: " + message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
} 