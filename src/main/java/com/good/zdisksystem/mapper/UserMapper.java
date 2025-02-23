package com.good.zdisksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.good.zdisksystem.entity.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    List<String> getUserRoles(Long userId);

    Long getRoleIdByCode(String code);

    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    User getUserById(Long userId);
}
