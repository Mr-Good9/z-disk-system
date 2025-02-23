package com.good.zdisksystem.service;

public interface EmailService {
    /**
     * 发送验证码邮件
     *
     * @param to 收件人
     * @param code 验证码
     */
    void sendVerificationCode(String to, String code);
} 