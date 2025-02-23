package com.good.zdisksystem.service.impl;

import com.good.zdisksystem.model.Friend;
import com.good.zdisksystem.mapper.FriendMapper;
import com.good.zdisksystem.service.IFriendService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 好友关系表 服务实现类
 * </p>
 *
 * @author chris
 * @since 2025-02-16
 */
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements IFriendService {

}
