package com.good.zdisksystem.service.impl;

import com.good.zdisksystem.model.MessageBoard;
import com.good.zdisksystem.mapper.MessageBoardMapper;
import com.good.zdisksystem.service.IMessageBoardService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 留言板表 服务实现类
 * </p>
 *
 * @author chris
 * @since 2025-02-16
 */
@Service
public class MessageBoardServiceImpl extends ServiceImpl<MessageBoardMapper, MessageBoard> implements IMessageBoardService {

}
