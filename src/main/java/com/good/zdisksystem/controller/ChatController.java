package com.good.zdisksystem.controller;

import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.common.utils.RequestUser;
import com.good.zdisksystem.entity.model.ChatMessage;
import com.good.zdisksystem.entity.model.ChatSession;
import com.good.zdisksystem.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

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
        Long userId = RequestUser.getUser().getId();
        chatService.markMessagesAsRead(userId, friendId);
        return CommonResult.success(null);
    }
}
