package com.good.zdisksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.good.zdisksystem.entity.model.Role;
import com.good.zdisksystem.entity.model.User;
import com.good.zdisksystem.entity.model.UserRole;
import com.good.zdisksystem.mapper.UserMapper;
import com.good.zdisksystem.mapper.UserRoleMapper;
import com.good.zdisksystem.service.IRoleService;
import com.good.zdisksystem.service.IUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private IRoleService roleService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Role getRoleNameByUserId(Long id) {
        UserRole userRole = lambdaQuery().eq(UserRole::getUserId, id).one();
        if (userRole != null) {
            return  roleService.getById(userRole.getRoleId());
        }
        return null;
    }

    @Override
    public void deleteByUserId(Long userId) {
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        boolean remove = lambdaUpdate().eq(UserRole::getUserId, userId)
                .remove();
        if (!remove) {
            throw new RuntimeException("删除失败");
        }
    }
}
