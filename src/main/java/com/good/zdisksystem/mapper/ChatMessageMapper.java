package com.good.zdisksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.good.zdisksystem.entity.model.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 聊天消息数据访问接口
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "fromUserId", column = "from_user_id"),
            @Result(property = "toUserId", column = "to_user_id"),
            @Result(property = "content", column = "content"),
            @Result(property = "type", column = "type"),
            @Result(property = "status", column = "status"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "senderNickname", column = "sender_nickname"),
            @Result(property = "senderUsername", column = "sender_username"),
            @Result(property = "senderAvatar", column = "sender_avatar"),
            @Result(property = "receiverNickname", column = "receiver_nickname"),
            @Result(property = "receiverUsername", column = "receiver_username"),
            @Result(property = "receiverAvatar", column = "receiver_avatar")
    })
    @Select("SELECT m.*, " +
            "COALESCE(sender.nickname, sender.username) as sender_nickname, " +
            "sender.username as sender_username, " +
            "sender.avatar as sender_avatar, " +
            "COALESCE(receiver.nickname, receiver.username) as receiver_nickname, " +
            "receiver.username as receiver_username, " +
            "receiver.avatar as receiver_avatar " +
            "FROM chat_message m " +
            "LEFT JOIN user sender ON m.from_user_id = sender.id " +
            "LEFT JOIN user receiver ON m.to_user_id = receiver.id " +
            "WHERE (m.from_user_id = #{userId} AND m.to_user_id = #{friendId}) " +
            "   OR (m.from_user_id = #{friendId} AND m.to_user_id = #{userId}) " +
            "ORDER BY m.create_time ASC")
    List<ChatMessage> getChatMessages(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * 将消息标记为已读
     *
     * @param userId   接收者ID
     * @param friendId 发送者ID
     * @return 更新的记录数
     */
    void markAsRead(@Param("userId") Long userId, @Param("friendId") Long friendId);
}
