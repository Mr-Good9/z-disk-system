// src/main/java/com/good/zdisksystem/service/impl/MessageBoardServiceImpl.java
package com.good.zdisksystem.service.impl;

import com.good.zdisksystem.common.exception.CustomException;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.dto.MessageBoardDTO;
import com.good.zdisksystem.dto.MessageBoardRequestDTO;
import com.good.zdisksystem.entity.model.MessageBoard;
import com.good.zdisksystem.entity.model.MessageLike;
import com.good.zdisksystem.mapper.MessageBoardMapper;
import com.good.zdisksystem.mapper.MessageLikeMapper;
import com.good.zdisksystem.service.MessageBoardService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageBoardServiceImpl implements MessageBoardService {

    @Autowired
    private MessageBoardMapper messageBoardMapper;

    @Autowired
    private MessageLikeMapper messageLikeMapper;

    @Override
    @Transactional
    public MessageBoardDTO addMessage(Long userId, MessageBoardRequestDTO requestDTO) {
        if (requestDTO.getContent() == null || requestDTO.getContent().trim().isEmpty()) {
            throw new CustomException("留言内容不能为空");
        }

        MessageBoard messageBoard = new MessageBoard();
        messageBoard.setUserId(userId);
        messageBoard.setContent(requestDTO.getContent().trim());
        messageBoard.setParentId(0L); // 主留言
        messageBoard.setCreateTime(LocalDateTime.now());
        messageBoard.setUpdateTime(LocalDateTime.now());

        messageBoardMapper.insert(messageBoard);

        // 查询刚插入的数据以获取用户信息
        return convertToDTO(messageBoardMapper.selectById(messageBoard.getId()), userId);
    }

    @Override
    @Transactional
    public MessageBoardDTO replyMessage(Long userId, Long messageId, MessageBoardRequestDTO requestDTO) {
        // 检查父留言是否存在
        MessageBoard parentMessage = messageBoardMapper.selectById(messageId);
        if (parentMessage == null || parentMessage.getStatus() != 1) {
            throw new CustomException("父留言不存在或已被删除");
        }

        // 如果父留言本身是回复，那么将回复指向原始留言
        Long realParentId = parentMessage.getParentId() == 0 ? messageId : parentMessage.getParentId();

        MessageBoard reply = new MessageBoard();
        reply.setUserId(userId);
        reply.setContent(requestDTO.getContent().trim());
        reply.setParentId(realParentId);
        reply.setCreateTime(LocalDateTime.now());
        reply.setUpdateTime(LocalDateTime.now());

        messageBoardMapper.insert(reply);

        // 查询刚插入的数据以获取用户信息
        return convertToDTO(messageBoardMapper.selectById(reply.getId()), userId);
    }

    @Override
    public PageResult<MessageBoardDTO> getMessages(Integer pageNum, Integer pageSize, String order, Long userId) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }

        int offset = (pageNum - 1) * pageSize;
        List<MessageBoard> messageList = messageBoardMapper.selectList(offset, pageSize, order);
        int total = messageBoardMapper.countTotal();

        List<MessageBoardDTO> dtoList = messageList.stream()
                .map(msg -> {
                    MessageBoardDTO dto = convertToDTO(msg, userId);
                    // 加载回复
                    List<MessageBoard> replies = messageBoardMapper.selectReplies(msg.getId());
                    dto.setReplies(replies.stream()
                            .map(reply -> convertToDTO(reply, userId))
                            .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
        PageResult<MessageBoardDTO> result = new PageResult<>();
        result.setTotal(total);
        result.setList(dtoList);
        return result;
    }

    @Override
    public MessageBoardDTO getMessage(Long messageId, Long userId) {
        MessageBoard message = messageBoardMapper.selectById(messageId);
        if (message == null || message.getStatus() != 1) {
            throw new CustomException("留言不存在或已被删除");
        }

        MessageBoardDTO dto = convertToDTO(message, userId);

        // 如果是主留言，加载回复
        if (message.getParentId() == 0) {
            List<MessageBoard> replies = messageBoardMapper.selectReplies(messageId);
            dto.setReplies(replies.stream()
                    .map(reply -> convertToDTO(reply, userId))
                    .collect(Collectors.toList()));
        } else {
            dto.setReplies(new ArrayList<>());
        }

        return dto;
    }

    @Override
    @Transactional
    public boolean likeMessage(Long userId, Long messageId) {
        // 检查留言是否存在
        MessageBoard message = messageBoardMapper.selectById(messageId);
        if (message == null || message.getStatus() != 1) {
            throw new CustomException("留言不存在或已被删除");
        }

        // 检查是否已点赞
        if (messageLikeMapper.isLiked(messageId, userId)) {
            return true; // 已点赞，直接返回成功
        }

        // 添加点赞记录
        MessageLike like = new MessageLike();
        like.setMessageId(messageId);
        like.setUserId(userId);
        messageLikeMapper.insert(like);

        // 更新点赞数
        messageBoardMapper.increaseLikeCount(messageId);

        return true;
    }

    @Override
    @Transactional
    public boolean unlikeMessage(Long userId, Long messageId) {
        // 检查是否已点赞
        if (!messageLikeMapper.isLiked(messageId, userId)) {
            return true; // 未点赞，直接返回成功
        }

        // 删除点赞记录
        messageLikeMapper.delete(messageId, userId);

        // 更新点赞数
        messageBoardMapper.decreaseLikeCount(messageId);

        return true;
    }

    @Override
    @Transactional
    public boolean deleteMessage(Long userId, Long messageId) {
        MessageBoard message = messageBoardMapper.selectById(messageId);
        if (message == null || message.getStatus() != 1) {
            throw new CustomException("留言不存在或已被删除");
        }

        // 检查删除权限（只能删除自己的留言）
        if (!message.getUserId().equals(userId)) {
            throw new CustomException("无权删除此留言");
        }

        // 软删除留言
        return messageBoardMapper.deleteById(messageId) > 0;
    }

    // 转换实体到DTO
    private MessageBoardDTO convertToDTO(MessageBoard message, Long currentUserId) {
        if (message == null) {
            return null;
        }

        MessageBoardDTO dto = new MessageBoardDTO();
        BeanUtils.copyProperties(message, dto);

        // 设置是否已点赞
        if (currentUserId != null) {
            dto.setIsLiked(messageLikeMapper.isLiked(message.getId(), currentUserId));
        } else {
            dto.setIsLiked(false);
        }

        return dto;
    }
}
