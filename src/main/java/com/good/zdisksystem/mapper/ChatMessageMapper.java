package com.good.zdisksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.good.zdisksystem.entity.model.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;

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
            @Result(property = "senderAvatar", column = "sender_avatar")
    })
    @Select("SELECT cm.*, " +
            "COALESCE(u.nickname, u.username) as sender_nickname, " +
            "u.username as sender_username, " +
            "u.avatar as sender_avatar " +
            "FROM chat_message cm " +
            "LEFT JOIN user u ON cm.from_user_id = u.id " +
            "WHERE (cm.from_user_id = #{userId} AND cm.to_user_id = #{friendId}) " +
            "   OR (cm.from_user_id = #{friendId} AND cm.to_user_id = #{userId}) " +
            "ORDER BY cm.create_time ASC")
    List<ChatMessage> getChatMessages(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * 将消息标记为已读
     *
     * @param toUserId   接收者ID
     * @param fromUserId 发送者ID
     * @return 更新的记录数
     */
    int markAsRead(@Param("toUserId") Long toUserId,
                   @Param("fromUserId") Long fromUserId);
}
