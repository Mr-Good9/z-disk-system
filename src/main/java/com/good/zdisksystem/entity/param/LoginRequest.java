package com.good.zdisksystem.entity.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;

/**
 * @author chris
 * @since 2024/11/26 19:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    /**
     * 登录类型: admin 或 user
     */
    private String loginType;
    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String captcha;
    /**
     * 验证码uuid
     */
    @NotBlank(message = "验证码标识不能为空")
    private String uuid;
    /**
     * IP地址
     */
    private String ip;
}
