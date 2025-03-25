package com.good.zdisksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.good.zdisksystem.entity.model.ChatMessage;
import com.good.zdisksystem.entity.model.ChatSession;
import com.good.zdisksystem.entity.model.User;
import com.good.zdisksystem.mapper.ChatMessageMapper;
import com.good.zdisksystem.mapper.ChatSessionMapper;
import com.good.zdisksystem.mapper.UserMapper;
import com.good.zdisksystem.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    private UserMapper userMapper;

    // 使用 ConcurrentHashMap 存储在线用户
    private static final ConcurrentHashMap<Long, LocalDateTime> onlineUsers = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public void sendMessage(ChatMessage message) {
        // 设置发送时间
        message.setCreateTime(LocalDateTime.now());
        message.setStatus(0); // 未读状态
        
        // 保存消息
        chatMessageMapper.insert(message);
        
        // 更新或创建会话
        updateChatSession(message);
    }

    @Override
    public List<ChatSession> getUserSessions(Long userId) {
        List<ChatSession> sessions = chatSessionMapper.getUserSessions(userId);
        
        // 确保每个会话都有完整的好友信息
        sessions.forEach(session -> {
            if (session.getFriendUsername() == null) {
                User friend = userMapper.selectById(session.getFriendId());
                if (friend != null) {
                    session.setFriendNickname(friend.getNickname());
                    session.setFriendUsername(friend.getUsername());
                    session.setFriendAvatar(friend.getAvatar());
                }
            }
        });
        
        return sessions;
    }

    @Override
    public List<ChatMessage> getChatMessages(Long userId, Long friendId) {
        // 获取聊天记录，现在已经包含了发送者的昵称和头像信息
        List<ChatMessage> messages = chatMessageMapper.getChatMessages(userId, friendId);
        
        log.info("获取到的消息列表:");
        messages.forEach(msg -> log.info("消息 - fromUserId: {}, toUserId: {}, content: {}, senderNickname: {}", 
            msg.getFromUserId(), msg.getToUserId(), msg.getContent(), msg.getSenderNickname()));
        
        return messages;
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long userId, Long friendId) {
        try {
            // 标记消息为已读
            chatMessageMapper.markAsRead(userId, friendId);
            
            // 更新会话的未读消息数
            chatSessionMapper.updateUnreadCount(userId, friendId, 0);
            
            log.info("已将用户 {} 接收自好友 {} 的消息标记为已读", userId, friendId);
        } catch (Exception e) {
            log.error("标记消息已读失败: userId={}, friendId={}", userId, friendId, e);
            throw new RuntimeException("标记消息已读失败", e);
        }
    }

    @Override
    public int getUnreadMessageCount(Long userId) {
        return Math.toIntExact(chatMessageMapper.selectCount(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getToUserId, userId)
                        .eq(ChatMessage::getStatus, 0)
        ));
    }

    @Override
    @Transactional
    public ChatSession createOrGetSession(Long userId, Long friendId) {
        // 先查找是否已存在会话
        ChatSession session = chatSessionMapper.selectOne(
            new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getFriendId, friendId)
        );
        
        if (session == null) {
            // 获取好友信息
            User friend = userMapper.selectById(friendId);
            if (friend == null) {
                throw new RuntimeException("好友不存在");
            }
            
            // 创建新会话
            session = new ChatSession();
            session.setUserId(userId);
            session.setFriendId(friendId);
            session.setUnreadCount(0);
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            // 设置好友信息
            session.setFriendNickname(friend.getNickname());
            session.setFriendUsername(friend.getUsername());
            session.setFriendAvatar(friend.getAvatar());
            chatSessionMapper.insert(session);
            
            // 同时为好友创建会话
            User currentUser = userMapper.selectById(userId);
            ChatSession friendSession = new ChatSession();
            friendSession.setUserId(friendId);
            friendSession.setFriendId(userId);
            friendSession.setUnreadCount(0);
            friendSession.setCreateTime(LocalDateTime.now());
            friendSession.setUpdateTime(LocalDateTime.now());
            // 设置当前用户信息（作为好友的好友信息）
            friendSession.setFriendNickname(currentUser.getNickname());
            friendSession.setFriendUsername(currentUser.getUsername());
            friendSession.setFriendAvatar(currentUser.getAvatar());
            chatSessionMapper.insert(friendSession);
        } else {
            // 如果会话存在但没有好友信息，更新好友信息
            if (session.getFriendUsername() == null) {
                User friend = userMapper.selectById(friendId);
                if (friend != null) {
                    session.setFriendNickname(friend.getNickname());
                    session.setFriendUsername(friend.getUsername());
                    session.setFriendAvatar(friend.getAvatar());
                    chatSessionMapper.updateById(session);
                }
            }
        }
        
        return session;
    }

    private void updateChatSession(ChatMessage message) {
        // 更新或创建发送方的会话
        updateOrCreateSession(message.getFromUserId(), message.getToUserId(), message);
        // 更新或创建接收方的会话
        updateOrCreateSession(message.getToUserId(), message.getFromUserId(), message);
    }

    private void updateOrCreateSession(Long userId, Long friendId, ChatMessage message) {
        ChatSession session = chatSessionMapper.selectOne(
            new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getFriendId, friendId)
        );
        
        if (session == null) {
            session = new ChatSession();
            session.setUserId(userId);
            session.setFriendId(friendId);
            
            // 获取好友信息
            User friend = userMapper.selectById(friendId);
            if (friend != null) {
                session.setFriendNickname(friend.getNickname());
                session.setFriendUsername(friend.getUsername());
                session.setFriendAvatar(friend.getAvatar());
            }
        }
        
        session.setLastMessage(message.getContent());
        session.setLastMessageTime(message.getCreateTime());
        session.setUnreadCount(session.getUnreadCount() != null ? session.getUnreadCount() + 1 : 1);
        
        if (session.getId() == null) {
            chatSessionMapper.insert(session);
        } else {
            chatSessionMapper.updateById(session);
        }
    }

    @Override
    public List<Long> getOnlineUsers() {
        // 清理超时用户（例如15分钟未活动）
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(15);
        onlineUsers.entrySet().removeIf(entry -> entry.getValue().isBefore(timeoutThreshold));
        
        return new ArrayList<>(onlineUsers.keySet());
    }

    // 添加用户上线方法
    public void userOnline(Long userId) {
        onlineUsers.put(userId, LocalDateTime.now());
        log.info("用户上线: {}", userId);
    }

    // 添加用户下线方法
    public void userOffline(Long userId) {
        onlineUsers.remove(userId);
        log.info("用户下线: {}", userId);
    }

    // 更新用户最后活动时间
    public void updateUserActivity(Long userId) {
        onlineUsers.put(userId, LocalDateTime.now());
    }
}
