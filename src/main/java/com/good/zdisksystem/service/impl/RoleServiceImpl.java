package com.good.zdisksystem.service.impl;

import com.good.zdisksystem.entity.model.Role;
import com.good.zdisksystem.entity.model.UserRole;
import com.good.zdisksystem.mapper.RoleMapper;
import com.good.zdisksystem.service.IRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.good.zdisksystem.service.IUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author chris
 * @since 2025-02-16
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

}
