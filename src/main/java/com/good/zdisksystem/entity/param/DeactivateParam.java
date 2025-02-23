package com.good.zdisksystem.entity.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DeactivateParam {
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "验证码不能为空")
    private String verifyCode;
} 