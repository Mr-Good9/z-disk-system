package com.good.zdisksystem.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    @GetMapping("/websocket-test")
    public Map<String, Object> websocketTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "WebSocket服务正常");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
} 