package com.good.zdisksystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.good.zdisksystem.entity.model.Role;
import com.good.zdisksystem.entity.model.UserRole;

/**
 * <p>
 * 用户角色关系表 服务类
 * </p>
 *
 * @author chris
 * @since 2025-02-16
 */
public interface IUserRoleService extends IService<UserRole> {

    Role getRoleNameByUserId(Long id);

    void deleteByUserId(Long userId);
}
