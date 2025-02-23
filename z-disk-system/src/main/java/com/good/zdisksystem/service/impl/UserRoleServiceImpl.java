package com.good.zdisksystem.service.impl;

import com.good.zdisksystem.model.UserRole;
import com.good.zdisksystem.mapper.UserRoleMapper;
import com.good.zdisksystem.service.IUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户角色关系表 服务实现类
 * </p>
 *
 * @author chris
 * @since 2025-02-16
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements IUserRoleService {

}
