package com.good.zdisksystem.websocket;

import com.good.zdisksystem.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketSessionManager {
    private final ConcurrentHashMap<Long, Set<String>> userSessions = new ConcurrentHashMap<>();
    private final ChatService chatService;

    public WebSocketSessionManager(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 添加用户会话
     * @param userId 用户ID
     * @param sessionId WebSocket会话ID
     */
    public void addSession(Long userId, String sessionId) {
        try {
            userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                       .add(sessionId);
            chatService.userOnline(userId);
            log.info("用户 {} 添加会话: {}", userId, sessionId);
        } catch (Exception e) {
            log.error("添加用户会话失败 - userId: {}, sessionId: {}", userId, sessionId, e);
        }
    }

    /**
     * 移除用户会话
     * @param userId 用户ID
     * @param sessionId WebSocket会话ID
     */
    public void removeSession(Long userId, String sessionId) {
        try {
            Set<String> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                // 如果用户没有任何活动会话，则标记为离线
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                    chatService.userOffline(userId);
                    log.info("用户 {} 所有会话已关闭，标记为离线", userId);
                } else {
                    log.info("用户 {} 移除会话: {}, 但仍有其他活动会话", userId, sessionId);
                }
            }
        } catch (Exception e) {
            log.error("移除用户会话失败 - userId: {}, sessionId: {}", userId, sessionId, e);
        }
    }

    /**
     * 检查用户是否在线
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(Long userId) {
        return userSessions.containsKey(userId) && !userSessions.get(userId).isEmpty();
    }

    /**
     * 获取用户的所有会话ID
     * @param userId 用户ID
     * @return 会话ID集合
     */
    public Set<String> getUserSessions(Long userId) {
        return userSessions.getOrDefault(userId, ConcurrentHashMap.newKeySet());
    }

    /**
     * 更新用户活动状态
     * @param userId 用户ID
     */
    public void updateUserActivity(Long userId) {
        if (isUserOnline(userId)) {
            chatService.updateUserActivity(userId);
            log.debug("更新用户 {} 活动状态", userId);
        }
    }
}
