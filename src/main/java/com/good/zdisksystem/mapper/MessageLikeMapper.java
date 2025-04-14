// src/main/java/com/good/zdisksystem/mapper/MessageLikeMapper.java
package com.good.zdisksystem.mapper;

import com.good.zdisksystem.entity.model.MessageLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MessageLikeMapper {

    // 插入点赞记录
    int insert(MessageLike messageLike);

    // 删除点赞记录
    int delete(@Param("messageId") Long messageId, @Param("userId") Long userId);

    // 查询点赞记录
    MessageLike select(@Param("messageId") Long messageId, @Param("userId") Long userId);

    // 查询用户是否点赞过指定留言
    boolean isLiked(@Param("messageId") Long messageId, @Param("userId") Long userId);
}
