// src/main/java/com/good/zdisksystem/service/impl/MessageBoardServiceImpl.java
package com.good.zdisksystem.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.good.zdisksystem.common.exception.CustomException;
import com.good.zdisksystem.common.exception.CustomException;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.dto.MessageBoardDTO;
import com.good.zdisksystem.dto.MessageBoardManagerDTO;
import com.good.zdisksystem.dto.MessageBoardRequestDTO;
import com.good.zdisksystem.entity.model.MessageBoard;
import com.good.zdisksystem.entity.model.MessageLike;
import com.good.zdisksystem.entity.model.User;
import com.good.zdisksystem.entity.vo.MessageBoardManagerDetailVO;
import com.good.zdisksystem.mapper.MessageBoardMapper;
import com.good.zdisksystem.mapper.MessageLikeMapper;
import com.good.zdisksystem.mapper.UserMapper;
import com.good.zdisksystem.service.MessageBoardService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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

    @Autowired
    private UserMapper userMapper;

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

    // 以下是管理员留言板功能实现
    @Override
    public PageResult<MessageBoardManagerDTO> getManagerMessageList(Integer pageNum, Integer pageSize,
                                                                  String keyword, Integer status, String startTime, String endTime) {
        // 使用PageHelper进行分页
        PageHelper.startPage(pageNum, pageSize);

        // 根据条件查询留言
        List<MessageBoard> messages = messageBoardMapper.selectMessagesByCondition(keyword, status, startTime, endTime);
        PageInfo<MessageBoard> pageInfo = new PageInfo<>(messages);

        // 转换为DTO
        List<MessageBoardManagerDTO> dtoList = messages.stream()
                .map(this::convertToManagerDTO)
                .collect(Collectors.toList());

        // 封装结果
        PageResult<MessageBoardManagerDTO> result = new PageResult<>();
        result.setTotal(pageInfo.getTotal());
        result.setList(dtoList);
        return result;
    }

    @Override
    @Transactional
    public void adminDeleteMessage(Long id) {
        // 检查留言是否存在
        MessageBoard message = messageBoardMapper.selectById(id);
        if (message == null) {
            throw new CustomException("留言不存在");
        }

        // 管理员可以删除任何留言，无需检查所有权
        messageBoardMapper.updateStatus(id, 0); // 0表示已删除
    }

    @Override
    @Transactional
    public void adminRestoreMessage(Long id) {
        // 检查留言是否存在
        MessageBoard message = messageBoardMapper.selectDeleteById(id);
        if (message == null) {
            throw new CustomException("留言不存在");
        }

        // 恢复留言
        messageBoardMapper.updateStatus(id, 1); // 1表示正常
    }

    @Override
    @Transactional
    public void adminBatchDelete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        // 批量删除留言
        messageBoardMapper.batchUpdateStatus(ids, 0);
    }

    @Override
    @Transactional
    public void adminBatchRestore(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        // 批量恢复留言
        messageBoardMapper.batchUpdateStatus(ids, 1);
    }

    @Override
    public MessageBoardManagerDetailVO getManagerMessageDetail(Long id) {
        // 查询留言详情
        MessageBoard message = messageBoardMapper.selectById(id);
        if (message == null) {
            throw new CustomException("留言不存在");
        }

        // 查询回复
        List<MessageBoard> replies = messageBoardMapper.selectReplies(id);

        // 转换为VO
        return convertToManagerDetailVO(message, replies);
    }

    // 将实体转换为管理员DTO
    private MessageBoardManagerDTO convertToManagerDTO(MessageBoard message) {
        if (message == null) {
            return null;
        }

        MessageBoardManagerDTO dto = new MessageBoardManagerDTO();
        BeanUtils.copyProperties(message, dto);

        // 获取用户信息
        User user = userMapper.selectById(message.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());
            dto.setAvatar(user.getAvatar());
//            dto.setUserType(user.getUserType());
        }

        // 获取回复数量
        int replyCount = messageBoardMapper.countReplies(message.getId());
        dto.setReplyCount(replyCount);

        return dto;
    }

    // 将实体转换为管理员详情VO
    private MessageBoardManagerDetailVO convertToManagerDetailVO(MessageBoard message, List<MessageBoard> replies) {
        if (message == null) {
            return null;
        }

        MessageBoardManagerDetailVO vo = new MessageBoardManagerDetailVO();
        BeanUtils.copyProperties(message, vo);

        // 获取用户信息
        User user = userMapper.selectById(message.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
//            vo.setUserType(user.getUserType());
        }

        // 转换回复列表
        List<MessageBoardManagerDTO> replyDtos = replies.stream()
                .map(this::convertToManagerDTO)
                .collect(Collectors.toList());
        vo.setReplies(replyDtos);

        return vo;
    }

    // 用于用户功能的DTO转换方法
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
