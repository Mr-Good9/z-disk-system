package com.good.zdisksystem.service.impl;

import com.good.zdisksystem.model.User;
import com.good.zdisksystem.mapper.UserMapper;
import com.good.zdisksystem.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author chris
 * @since 2025-02-16
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
