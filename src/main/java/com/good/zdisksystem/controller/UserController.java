package com.good.zdisksystem.controller;

import com.good.zdisksystem.annotation.OperationLogger;
import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.entity.param.ChangePasswordParam;
import com.good.zdisksystem.entity.param.EmailCodeParam;
import com.good.zdisksystem.entity.param.UpdateUserParam;
import com.good.zdisksystem.entity.param.DeactivateParam;
import com.good.zdisksystem.entity.vo.UserInfoVo;
import com.good.zdisksystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public CommonResult<UserInfoVo> getUserInfo() {
        return CommonResult.success(userService.getCurrentUserInfo());
    }

    @PutMapping("/update")
    @OperationLogger(module = "用户", action = "更新", detail = "更新用户信息")
    public CommonResult<UserInfoVo> updateUserInfo(@RequestBody @Validated UpdateUserParam param) {
        return CommonResult.success(userService.updateUserInfo(param));
    }

    @PostMapping("/avatar")
    @OperationLogger(module = "用户", action = "更新", detail = "更新用户头像")
    public CommonResult<String> updateAvatar(@RequestParam("file") MultipartFile file) {
        return CommonResult.success(userService.updateAvatar(file));
    }

    @PostMapping("/email/code")
    public CommonResult<Void> sendEmailCode(@RequestBody @Validated EmailCodeParam param) {
        userService.sendEmailCode(param.getEmail());
        return CommonResult.success("验证码已发送");
    }

    @PutMapping("/password")
    @OperationLogger(module = "用户", action = "更新", detail = "修改用户密码")
    public CommonResult<Void> changePassword(@RequestBody @Validated ChangePasswordParam param) {
        userService.changePassword(param.getOldPassword(), param.getNewPassword());
        return CommonResult.success("密码修改成功");
    }

    @PostMapping("/deactivate")
    @OperationLogger(module = "用户", action = "更新", detail = "注销用户账号")
    public CommonResult<Void> deactivateAccount(@RequestBody @Valid DeactivateParam param) {
        userService.deactivateAccount(param.getPassword(), param.getVerifyCode());
        return CommonResult.success("账号已注销");
    }
}
