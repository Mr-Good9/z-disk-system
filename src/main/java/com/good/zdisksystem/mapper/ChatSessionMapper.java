package com.good.zdisksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.good.zdisksystem.entity.model.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
    
    /**
     * 获取用户的会话列表
     */
    @Select("SELECT cs.*, u.nickname as friend_nickname, u.username as friend_username, " +
            "u.avatar as friend_avatar " +
            "FROM chat_session cs " +
            "LEFT JOIN user u ON cs.friend_id = u.id " +
            "WHERE cs.user_id = #{userId} " +
            "ORDER BY cs.last_message_time DESC")
    List<ChatSession> getUserSessions(@Param("userId") Long userId);
    
    /**
     * 更新会话的未读消息数
     */
    int updateUnreadCount(@Param("userId") Long userId, 
                         @Param("friendId") Long friendId,
                         @Param("count") Integer count);
} 