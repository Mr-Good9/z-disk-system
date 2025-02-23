package com.good.zdisksystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.good.zdisksystem.entity.model.ChatMessage;
import com.good.zdisksystem.entity.model.ChatSession;
import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService extends IService<ChatMessage> {

    /**
     * 发送聊天消息
     * @param message 消息内容
     */
    void sendMessage(ChatMessage message);

    /**
     * 获取用户的会话列表
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSession> getUserSessions(Long userId);

    /**
     * 获取与指定好友的聊天记录
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 聊天记录列表
     */
    List<ChatMessage> getChatMessages(Long userId, Long friendId);

    /**
     * 标记消息为已读
     * @param userId 用户ID
     * @param friendId 好友ID
     */
    void markMessagesAsRead(Long userId, Long friendId);

    /**
     * 获取未读消息数量
     * @param userId 用户ID
     * @return 未读消息数量
     */
    int getUnreadMessageCount(Long userId);

    /**
     * 创建或获取会话
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 会话信息
     */
    ChatSession createOrGetSession(Long userId, Long friendId);

}
