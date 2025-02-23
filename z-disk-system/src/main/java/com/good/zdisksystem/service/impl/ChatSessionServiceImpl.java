package com.good.zdisksystem.service.impl;

import com.good.zdisksystem.model.ChatSession;
import com.good.zdisksystem.mapper.ChatSessionMapper;
import com.good.zdisksystem.service.IChatSessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会话列表表 服务实现类
 * </p>
 *
 * @author chris
 * @since 2025-02-16
 */
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements IChatSessionService {

}
