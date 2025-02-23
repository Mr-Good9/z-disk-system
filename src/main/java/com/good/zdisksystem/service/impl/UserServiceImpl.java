package com.good.zdisksystem.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.druid.sql.visitor.functions.If;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.injector.methods.UpdateById;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.good.zdisksystem.cache.AuthUserCache;
import com.good.zdisksystem.common.exception.CustomException;
import com.good.zdisksystem.common.exception.enums.GlobalErrorCodeConstants;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.common.utils.RequestUser;
import com.good.zdisksystem.entity.model.Role;
import com.good.zdisksystem.entity.model.UserRole;
import com.good.zdisksystem.entity.param.UpdateUserParam;
import com.good.zdisksystem.entity.param.UserQueryParam;
import com.good.zdisksystem.model.dto.UserUpdateDTO;
import com.good.zdisksystem.security.utils.JwtUtils;
import com.good.zdisksystem.entity.model.User;
import com.good.zdisksystem.entity.param.LoginRequest;
import com.good.zdisksystem.entity.param.RegisterRequest;
import com.good.zdisksystem.entity.vo.AuthResponse;
import com.good.zdisksystem.entity.vo.UserInfoVo;
import com.good.zdisksystem.entity.vo.UserVO;
import com.good.zdisksystem.mapper.UserMapper;
import com.good.zdisksystem.service.IRoleService;
import com.good.zdisksystem.service.IUserRoleService;
import com.good.zdisksystem.service.UserService;
import com.good.zdisksystem.service.EmailService;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AuthUserCache authUserCache;

    @Autowired
    private EmailService emailService;

    @Autowired
    private IUserRoleService userRoleService;

    @Override
    public User findByUsername(String username) {
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username)
        );
        if (user != null) {
            user.setRoles(getUserRoles(user.getId()));
        }
        return user;
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        // 验证用户类型
        User user = findByUsername(loginRequest.getUsername());
        if (user != null) {
            List<String> roles = getUserRoles(user.getId());
            if ("admin".equals(loginRequest.getLoginType()) && !roles.contains("ADMIN")) {
                throw new CustomException(GlobalErrorCodeConstants.FORBIDDEN);
            }
        }
        String token = "";
        // 验证用户名密码
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            User authenticatedUser = (User) authentication.getPrincipal();
            token = jwtUtils.generateToken(authenticatedUser);
            authUserCache.setAccessToken(user.getPhone(), token);
            // 更新登录信息
            updateLoginInfo(authenticatedUser.getId(), loginRequest.getIp());
        } catch (Exception e) {
            throw new CustomException(GlobalErrorCodeConstants.LOGIN_ERROR);
        }
        return new AuthResponse(token);
    }

    @Override
    @Transactional
    public User register(RegisterRequest registerRequest) {
        // 参数格式验证（虽然有注解验证，这里可以添加更复杂的验证逻辑）
        if (!registerRequest.getUsername().matches("^[a-zA-Z0-9_-]{4,20}$")) {
            throw new CustomException(GlobalErrorCodeConstants.INVALID_USERNAME_FORMAT);
        }
//        if (!registerRequest.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,20}$")) {
//            throw new CustomException(GlobalErrorCodeConstants.INVALID_PASSWORD_FORMAT);
//        }
        // 检查用户名是否已存在
        if (findByUsername(registerRequest.getUsername()) != null) {
            throw new CustomException(GlobalErrorCodeConstants.REGISTERED_ERROR);
        }
        // 检查邮箱是否已被使用
        if (!StringUtil.isNullOrEmpty(registerRequest.getEmail())) {
            if (userMapper.selectOne(new QueryWrapper<User>().eq("email", registerRequest.getEmail())) != null) {
                throw new CustomException(GlobalErrorCodeConstants.EMAIL_REGISTERED);
            }
        }
        // 检查手机号是否已被使用
        if (userMapper.selectOne(new QueryWrapper<User>().eq("phone", registerRequest.getPhone())) != null) {
            throw new CustomException(GlobalErrorCodeConstants.PHONE_REGISTERED);
        }

        // 创建用户实体
        User user = BeanUtil.toBean(registerRequest, User.class);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        // 设置默认值
        user.setStatus(1);
        user.setStorageUsed(0L);
        user.setStorageMax(5368709120L); // 5GB

        // 保存用户
        userMapper.insert(user);

        // 分配默认角色
        Long roleId = userMapper.getRoleIdByCode("USER");
        userMapper.insertUserRole(user.getId(), roleId);

        // 设置角色列表
        user.setRoles(Collections.singletonList("USER"));

        return user;
    }

    @Override
    public void updateLoginInfo(Long userId, String ip) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.updateById(user);
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        return userMapper.getUserRoles(userId);
    }

    @Override
    public UserInfoVo getCurrentUserInfo() {
        User user = findByUsername(RequestUser.getUser().getUsername());
        return convertToUserInfoVo(user);
    }

    @Override
    @Transactional
    public UserInfoVo updateUserInfo(UpdateUserParam param) {
        User user = findByUsername(RequestUser.getUser().getUsername());

        // 邮箱变更
        if (ObjectUtil.isNotNull(param.getEmail()) && !user.getEmail().equals(param.getEmail())) {
            // 检查邮箱是否已被其他用户使用
            User existUser = userMapper.selectOne(
                    new QueryWrapper<User>()
                            .eq("email", param.getEmail())
                            .ne("id", user.getId())
            );
            if (existUser != null) {
                throw new CustomException(GlobalErrorCodeConstants.EMAIL_REGISTERED);
            }

            String cachedCode = authUserCache.getCaptcha(param.getEmail());
            if (param.getVerifyCode() == null || !param.getVerifyCode().equals(cachedCode)) {
                throw new CustomException(GlobalErrorCodeConstants.CAPTCHA_ERROR);
            }
            authUserCache.delCaptcha(param.getEmail());
            user.setEmail(param.getEmail());
        }
        // 昵称变更
        if (ObjectUtil.isNotNull(param.getNickname()) && !user.getNickname().equals(param.getNickname())) {
            user.setNickname(param.getNickname());
        }
        // 手机号变更
        if (ObjectUtil.isNotNull(param.getPhone()) && !user.getPhone().equals(param.getPhone())) {
            // 检查手机号是否已被其他用户使用
            User existUser = userMapper.selectOne(
                    new QueryWrapper<User>()
                            .eq("phone", param.getPhone())
                            .ne("id", user.getId())
            );
            if (existUser != null) {
                throw new CustomException(GlobalErrorCodeConstants.PHONE_REGISTERED);
            }
            // TODO 获取手机号验证码 - 待删除
            String cachedCode = authUserCache.getCaptcha(param.getPhone());
            if (param.getVerifyCode() == null || !param.getVerifyCode().equals(cachedCode)) {
                throw new CustomException(GlobalErrorCodeConstants.CAPTCHA_ERROR);
            }
        }

        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return convertToUserInfoVo(user);
    }

    @Override
    public String updateAvatar(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException(GlobalErrorCodeConstants.FILE_IS_EMPTY);
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(GlobalErrorCodeConstants.FILE_TYPE_ERROR);
        }

        // 检查文件大小（假设限制为2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new CustomException(GlobalErrorCodeConstants.FILE_SIZE_EXCEED);
        }

        // TODO: 调用文件服务上传头像
        String avatarUrl = ""; // 文件服务返回的URL

        User user = findByUsername(RequestUser.getUser().getUsername());
        user.setAvatar(avatarUrl);
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);

        return avatarUrl;
    }

    @Override
    public void sendEmailCode(String email) {
        // 生成6位验证码
        String code = String.format("%06d", (int) (Math.random() * 1000000));

        // 将验证码存入缓存
        authUserCache.setCaptcha(email, code);

        // 发送验证码邮件
        emailService.sendVerificationCode(email, code);
    }

    @Override
    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        User user = findByUsername(RequestUser.getUser().getUsername());

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new CustomException(GlobalErrorCodeConstants.OLD_PASSWORD_ERROR);
        }

        // 检查新密码格式
        if (!newPassword.matches("^[a-zA-Z0-9_-]{6,20}$")) {
            throw new CustomException(GlobalErrorCodeConstants.INVALID_PASSWORD_FORMAT);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);

        // 清除登录token，强制重新登录
        authUserCache.delAccessToken(user.getPhone());
    }

    @Override
    public void deactivateAccount(String password, String verifyCode) {
        // 获取当前用户
        User user = findByUsername(RequestUser.getUser().getUsername());

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException(GlobalErrorCodeConstants.OLD_PASSWORD_ERROR);
        }

        // 验证邮箱验证码
        String cachedCode = authUserCache.getCaptcha(user.getEmail());
        if (verifyCode == null || !verifyCode.equals(cachedCode)) {
            throw new CustomException(GlobalErrorCodeConstants.CAPTCHA_ERROR);
        }

        // 注销账号（这里可以根据需求选择直接删除或者标记为已注销）
        user.setStatus(0); // 假设 0 表示已注销(禁用状态)
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 清除用户相关的缓存
        authUserCache.delAccessToken(user.getPhone());
        authUserCache.delCaptcha(user.getEmail());
    }

    @Override
    public PageResult<UserVO> getUserList(UserQueryParam param) {
        // 设置分页
        PageHelper.startPage(param.getPageNum(), param.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(param.getKeyword())) {
            wrapper.like(User::getUsername, param.getKeyword())
                  .or()
                  .like(User::getNickname, param.getKeyword())
                  .or()
                  .like(User::getEmail, param.getKeyword());
        }
        if (param.getStatus() != null) {
            wrapper.eq(User::getStatus, param.getStatus());
        }
//        if (StringUtils.hasText(param.getRole())) {
//            wrapper.eq(User::getRole, param.getRole());
//        }

        // 查询数据
        Page<User> page = userMapper.selectPage(new Page<>(param.getPageNum(), param.getPageSize()), wrapper);

        // 转换为VO
        List<UserVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        // 填充角色信息
        voList.forEach(userVO ->{
            Role role = userRoleService.getRoleNameByUserId(userVO.getId());
            if (role != null){
                userVO.setRole(role.getName());
            } else {
                userVO.setRole("普通用户"); // 默认角色
                UserRole userRole = new UserRole();
                userRole.setRoleId(2L); // 默认角色ID
                userRole.setUserId(userVO.getId());
                userRoleService.updateById(userRole);
            }
        } );

        return PageResult.build(voList, voList.size(), param.getPageNum(), param.getPageSize());
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        Assert.notNull(user, "用户不存在");
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        Assert.notNull(user, "用户不存在");

        // 删除用户相关数据
        userMapper.deleteById(userId);
        // 删除对应用户角色关系表
        userRoleService.deleteByUserId(userId);
    }

    @Override
    public void resetUserPassword(Long userId, String newPassword) {
        User user = userMapper.selectById(userId);
        Assert.notNull(user, "用户不存在");

        // 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userMapper.updateById(user);
    }

    @Override
    public void updateUserRole(Long userId, String role) {
        User user = userMapper.selectById(userId);
        Assert.notNull(user, "用户不存在");

        userMapper.updateById(user);
    }

    @Override
    public Long countTotalUsers() {
        return userMapper.selectCount(null);
    }

    @Override
    public Long countActiveUsers() {
        // 获取最近30天有登录记录的用户数
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(User::getLastLoginTime, thirtyDaysAgo);
        return userMapper.selectCount(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long userId, UserUpdateDTO userUpdate) {
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new CustomException("用户不存在");
        }

        // 更新基本信息
        user.setNickname(userUpdate.getNickname());
        user.setEmail(userUpdate.getEmail());
        userMapper.updateById(user);

        // 如果角色发生变化，更新用户角色
        if (StringUtils.hasText(userUpdate.getRole())) {
            // 获取新角色ID
            Long roleId = userMapper.getRoleIdByCode(userUpdate.getRole());
            if (roleId == null) {
                throw new CustomException("角色不存在");
            }

            // 更新用户角色关系
            UpdateWrapper<UserRole> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id", userId);
            UserRole userRole = new UserRole();
            userRole.setRoleId(roleId);
            userRoleService.update(userRole, updateWrapper);
        }
    }

    private UserInfoVo convertToUserInfoVo(User user) {
        UserInfoVo vo = new UserInfoVo();
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setCreateTime(user.getCreateTime().toString());
        vo.setUpdateTime(user.getUpdateTime().toString());
        vo.setStatus(String.valueOf(user.getStatus()));
        vo.setRoles(getUserRoles(user.getId()));

        // 设置存储信息
        UserInfoVo.StorageInfo storage = new UserInfoVo.StorageInfo();
        storage.setUsed(user.getStorageUsed());
        storage.setTotal(user.getStorageMax());
        vo.setStorage(storage);

        return vo;
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        // 设置存储信息等额外属性
        return vo;
    }

    @Override
    public User getByUserId(Long userId) {
        return lambdaQuery().eq(User::getId, userId).one();
    }
}
