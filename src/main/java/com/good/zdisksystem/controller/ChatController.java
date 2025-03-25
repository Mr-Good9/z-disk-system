package com.good.zdisksystem.controller;

import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.common.utils.RequestUser;
import com.good.zdisksystem.entity.model.ChatMessage;
import com.good.zdisksystem.entity.model.ChatSession;
import com.good.zdisksystem.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @GetMapping("/sessions")
    public CommonResult<List<ChatSession>> getChatSessions() {
        Long userId = RequestUser.getUser().getId();
        List<ChatSession> sessions = chatService.getUserSessions(userId);
        return CommonResult.success(sessions);
    }

    @PostMapping("/sessions/{friendId}")
    public CommonResult<ChatSession> createSession(@PathVariable Long friendId) {
        Long userId = RequestUser.getUser().getId();
        ChatSession session = chatService.createOrGetSession(userId, friendId);
        return CommonResult.success(session);
    }

    @GetMapping("/messages/{friendId}")
    public CommonResult<List<ChatMessage>> getChatMessages(@PathVariable Long friendId) {
        Long userId = RequestUser.getUser().getId();
        log.info("获取聊天记录 - userId: {}, friendId: {}", userId, friendId);
        List<ChatMessage> messages = chatService.getChatMessages(userId, friendId);
        log.info("聊天记录数量: {}", messages.size());
        messages.forEach(msg -> log.info("消息 - fromUserId: {}, toUserId: {}, content: {}",
            msg.getFromUserId(), msg.getToUserId(), msg.getContent()));
        return CommonResult.success(messages);
    }

    @PostMapping("/messages")
    public CommonResult<Void> sendMessage(@RequestBody ChatMessage message) {
        Long userId = RequestUser.getUser().getId();
        message.setFromUserId(userId);
        message.setCreateTime(LocalDateTime.now());
        message.setStatus(0); // 0-未读

        chatService.sendMessage(message);

        // 通过WebSocket发送消息给接收者
        messagingTemplate.convertAndSendToUser(
            message.getToUserId().toString(),
            "/queue/messages",
            message
        );

        return CommonResult.success(null);
    }

    @GetMapping("/unread/count")
    public CommonResult<Integer> getUnreadCount() {
        Long userId = RequestUser.getUser().getId();
        int count = chatService.getUnreadMessageCount(userId);
        return CommonResult.success(count);
    }

    @PostMapping("/messages/{friendId}/read")
    public CommonResult<Void> markMessagesAsRead(@PathVariable Long friendId) {
        try {
            Long userId = RequestUser.getUser().getId();
            log.info("标记消息已读: userId={}, friendId={}", userId, friendId);
            chatService.markMessagesAsRead(userId, friendId);
            return CommonResult.success(null);
        } catch (Exception e) {
            log.error("标记消息已读失败", e);
            return CommonResult.error("标记消息已读失败");
        }
    }

    @MessageMapping("/status")
    public void updateStatus(Principal principal) {
        if (principal == null) {
            log.warn("Principal is null in updateStatus");
            return;
        }
        
        try {
            Long userId = Long.parseLong(principal.getName());
            Map<String, Object> statusMessage = new HashMap<>();
            statusMessage.put("userId", userId);
            statusMessage.put("online", true);
            statusMessage.put("timestamp", LocalDateTime.now());
            
            // 广播用户上线消息
            messagingTemplate.convertAndSend("/topic/status", statusMessage);
            log.info("用户上线状态已广播: {}", userId);
        } catch (NumberFormatException e) {
            log.error("解析用户ID失败: {}", principal.getName(), e);
        } catch (Exception e) {
            log.error("处理用户状态更新失败", e);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = sha.getUser();
        
        if (principal == null) {
            log.warn("Principal is null in handleSessionDisconnect");
            return;
        }
        
        try {
            String userId = principal.getName();
            Map<String, Object> statusMessage = new HashMap<>();
            statusMessage.put("userId", userId);
            statusMessage.put("online", false);
            statusMessage.put("timestamp", LocalDateTime.now());
            
            // 广播用户下线消息
            messagingTemplate.convertAndSend("/topic/status", statusMessage);
            log.info("用户下线状态已广播: {}", userId);
        } catch (Exception e) {
            log.error("处理用户断开连接失败", e);
        }
    }

    @MessageMapping("/chat")
    public void handleChatMessage(Principal principal, ChatMessage message) {
        if (principal == null || message == null) {
            log.warn("Invalid chat message request");
            return;
        }

        try {
            Long senderId = Long.parseLong(principal.getName());
            message.setFromUserId(senderId);
            message.setCreateTime(LocalDateTime.now());
            message.setStatus(0); // 未读状态

            // 保存消息
            chatService.sendMessage(message);

            // 发送给接收者
            messagingTemplate.convertAndSendToUser(
                message.getToUserId().toString(),
                "/queue/messages",
                message
            );

            log.info("消息已发送: from={}, to={}", senderId, message.getToUserId());
        } catch (Exception e) {
            log.error("处理聊天消息失败", e);
        }
    }

    @GetMapping("/online-users")
    public CommonResult<List<Long>> getOnlineUsers() {
        try {
            List<Long> onlineUsers = chatService.getOnlineUsers();
            return CommonResult.success(onlineUsers);
        } catch (Exception e) {
            log.error("获取在线用户列表失败", e);
            return CommonResult.error("获取在线用户列表失败");
        }
    }
}
