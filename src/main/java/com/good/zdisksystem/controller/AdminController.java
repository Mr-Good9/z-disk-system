package com.good.zdisksystem.controller;

import com.good.zdisksystem.annotation.OperationLogger;
import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.entity.param.UserQueryParam;
import com.good.zdisksystem.entity.vo.StorageUsageVO;
import com.good.zdisksystem.entity.vo.UserVO;
import com.good.zdisksystem.entity.param.SystemSettingsDTO;
import com.good.zdisksystem.entity.param.UserUpdateDTO;
import com.good.zdisksystem.entity.vo.SystemSettingsVO;
import com.good.zdisksystem.entity.vo.SystemStatisticsVO;
import com.good.zdisksystem.service.FileService;
import com.good.zdisksystem.service.SystemService;
import com.good.zdisksystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
//@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Autowired
    private SystemService systemService;

    /**
     * 获取用户列表
     *
     * @param param
     * @return
     */
    @GetMapping("/users")
    public CommonResult<PageResult<UserVO>> getUserList(UserQueryParam param) {
        PageResult<UserVO> result = userService.getUserList(param);
        return CommonResult.success(result);
    }

    /**
     * 更新用户状态
     *
     * @param userId
     * @param status
     * @return
     */
    @PutMapping("/users/{userId}/status")
    @OperationLogger(module = "管理员", action = "更新", detail = "更新用户状态")
    public CommonResult<Void> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status) {
        userService.updateUserStatus(userId, status);
        return CommonResult.success(null);
    }

    /**
     * 删除用户
     *
     * @param userId
     * @return
     */
    @DeleteMapping("/users/{userId}")
    @OperationLogger(module = "管理员", action = "删除", detail = "删除用户")
    public CommonResult<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return CommonResult.success(null);
    }

    /**
     * 重置用户密码
     * 重置后默认密码为 123456
     *
     * @param userId
     * @param newPassword
     * @return
     */
    @PutMapping("/users/{userId}/password/reset")
    @OperationLogger(module = "管理员", action = "更新", detail = "重置用户密码")
    public CommonResult<Void> resetUserPassword(@PathVariable Long userId, @RequestParam String newPassword) {
        userService.resetUserPassword(userId, newPassword);
        return CommonResult.success(null);
    }

    /**
     * 更新用户角色
     *
     * @param userId
     * @param role
     * @return
     */
    @PutMapping("/users/{userId}/role")
    @OperationLogger(module = "管理员", action = "更新", detail = "更新用户角色")
    public CommonResult<Void> updateUserRole(@PathVariable Long userId, @RequestParam String role) {
        userService.updateUserRole(userId, role);
        return CommonResult.success(null);
    }

    /**
     * 获取系统统计信息
     *
     * @return
     */
    @GetMapping("/statistics")
    public CommonResult<SystemStatisticsVO> getSystemStatistics() {
        SystemStatisticsVO statistics = systemService.getStatistics();
        return CommonResult.success(statistics);
    }

    /**
     * 获取存储空间使用情况
     *
     * @return
     */
    @GetMapping("/storage/usage")
    public CommonResult<StorageUsageVO> getStorageUsage() {
        StorageUsageVO usage = fileService.getStorageUsage();
        return CommonResult.success(usage);
    }

    /**
     * 更新系统设置
     *
     * @param settings
     * @return
     */
    @PutMapping("/settings")
    public CommonResult<Void> updateSystemSettings(@RequestBody SystemSettingsDTO settings) {
        systemService.updateSettings(settings);
        return CommonResult.success(null);
    }

    /**
     * 获取系统设置
     *
     * @return
     */
    @GetMapping("/settings")
    public CommonResult<SystemSettingsVO> getSystemSettings() {
        SystemSettingsVO settings = systemService.getSettings();
        return CommonResult.success(settings);
    }

    /**
     * 更新用户信息
     *
     * @param userId
     * @param userUpdate
     * @return
     */
    @PutMapping("/users/{userId}")
    @OperationLogger(module = "管理员", action = "更新", detail = "更新用户信息")
    public CommonResult<Void> updateUser(@PathVariable Long userId, @RequestBody UserUpdateDTO userUpdate) {
        userService.updateUser(userId, userUpdate);
        return CommonResult.success(null);
    }

    // ... 其他管理接口
}
