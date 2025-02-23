package com.good.zdisksystem.entity.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class ChangePasswordParam {
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;
    
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{6,20}$", message = "密码必须为6-20位字母、数字、下划线")
    private String newPassword;
} 