package com.good.zdisksystem.common.exception;


import org.springframework.security.core.AuthenticationException;

/**
 * 登录验证码错误异常
 */
public class CaptchaException extends AuthenticationException {

    public CaptchaException(String msg) {
        super(msg);
    }

}
