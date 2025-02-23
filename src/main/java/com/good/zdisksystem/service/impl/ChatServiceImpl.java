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

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    private UserMapper userMapper;

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
        return chatSessionMapper.getUserSessions(userId);
    }

    @Override
    public List<ChatMessage> getChatMessages(Long userId, Long friendId) {
        List<ChatMessage> messages = chatMessageMapper.getChatMessages(userId, friendId);
        
        // 获取用户信息
        User currentUser = userMapper.selectById(userId);
        User friendUser = userMapper.selectById(friendId);
        
        // 设置发送者信息
        messages.forEach(msg -> {
            if (msg.getFromUserId().equals(userId)) {
                msg.setSenderNickname(currentUser.getNickname() != null ? 
                    currentUser.getNickname() : currentUser.getUsername());
                msg.setSenderAvatar(currentUser.getAvatar());
            } else {
                msg.setSenderNickname(friendUser.getNickname() != null ? 
                    friendUser.getNickname() : friendUser.getUsername());
                msg.setSenderAvatar(friendUser.getAvatar());
            }
        });
        
        return messages;
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long userId, Long friendId) {
        // 将消息标记为已读
        chatMessageMapper.markAsRead(userId, friendId);
        
        // 更新会话的未读消息数
        chatSessionMapper.updateUnreadCount(userId, friendId, 0);
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
            // 创建新会话
            session = new ChatSession();
            session.setUserId(userId);
            session.setFriendId(friendId);
            session.setUnreadCount(0);
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            chatSessionMapper.insert(session);
            
            // 同时为好友创建会话
            ChatSession friendSession = new ChatSession();
            friendSession.setUserId(friendId);
            friendSession.setFriendId(userId);
            friendSession.setUnreadCount(0);
            friendSession.setCreateTime(LocalDateTime.now());
            friendSession.setUpdateTime(LocalDateTime.now());
            chatSessionMapper.insert(friendSession);
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
}
