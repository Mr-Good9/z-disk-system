package com.good.zdisksystem.service.impl;

import com.good.zdisksystem.model.ChatMessage;
import com.good.zdisksystem.mapper.ChatMessageMapper;
import com.good.zdisksystem.service.IChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 聊天消息表 服务实现类
 * </p>
 *
 * @author chris
 * @since 2025-02-16
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

}
