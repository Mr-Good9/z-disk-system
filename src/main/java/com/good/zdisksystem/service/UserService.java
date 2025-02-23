package com.good.zdisksystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.entity.model.User;
import com.good.zdisksystem.entity.param.LoginRequest;
import com.good.zdisksystem.entity.param.RegisterRequest;
import com.good.zdisksystem.entity.param.UpdateUserParam;
import com.good.zdisksystem.entity.param.UserQueryParam;
import com.good.zdisksystem.entity.vo.AuthResponse;
import com.good.zdisksystem.entity.vo.UserInfoVo;
import com.good.zdisksystem.entity.vo.UserVO;
import com.good.zdisksystem.model.dto.UserUpdateDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService extends IService<User> {

    User findByUsername(String username);

    AuthResponse login(LoginRequest loginRequest);

    User register(RegisterRequest registerRequest);

    void updateLoginInfo(Long userId, String ip);

    List<String> getUserRoles(Long userId);

    UserInfoVo getCurrentUserInfo();

    UserInfoVo updateUserInfo(UpdateUserParam param);

    String updateAvatar(MultipartFile file);

    void sendEmailCode(String email);

    void changePassword(String oldPassword, String newPassword);

    void deactivateAccount(String password, String verifyCode);

    /**
     * 获取用户列表
     */
    PageResult<UserVO> getUserList(UserQueryParam param);

    /**
     * 更新用户状态
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 删除用户
     */
    void deleteUser(Long userId);

    /**
     * 重置用户密码
     */
    void resetUserPassword(Long userId, String newPassword);

    /**
     * 更新用户角色
     */
    void updateUserRole(Long userId, String role);

    /**
     * 获取总用户数
     */
    Long countTotalUsers();

    /**
     * 获取活跃用户数
     */
    Long countActiveUsers();

    /**
     * 更新用户信息
     */
    void updateUser(Long userId, UserUpdateDTO userUpdate);

    User getByUserId(Long userId);
}
