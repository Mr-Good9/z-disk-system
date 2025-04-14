// src/main/java/com/good/zdisksystem/service/MessageBoardService.java
package com.good.zdisksystem.service;

import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.dto.MessageBoardDTO;
import com.good.zdisksystem.dto.MessageBoardRequestDTO;

public interface MessageBoardService {

    // 添加留言
    MessageBoardDTO addMessage(Long userId, MessageBoardRequestDTO requestDTO);

    // 回复留言
    MessageBoardDTO replyMessage(Long userId, Long messageId, MessageBoardRequestDTO requestDTO);

    // 获取留言列表（分页）
    PageResult<MessageBoardDTO> getMessages(Integer pageNum, Integer pageSize, String order, Long userId);

    // 获取留言详情
    MessageBoardDTO getMessage(Long messageId, Long userId);

    // 点赞留言
    boolean likeMessage(Long userId, Long messageId);

    // 取消点赞
    boolean unlikeMessage(Long userId, Long messageId);

    // 删除留言
    boolean deleteMessage(Long userId, Long messageId);
}
