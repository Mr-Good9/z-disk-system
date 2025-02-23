package com.good.zdisksystem.service.impl;

import com.good.zdisksystem.common.exception.CustomException;
import com.good.zdisksystem.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendVerificationCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("验证码");

            String content = String.format(
                "<html><body>" +
                "<h2>网盘分享系统</h2>" +
                "<h3>您的验证码是：%s</h3>" +
                "<p>验证码有效期为5分钟，请尽快使用。</p>" +
                "<p>如果这不是您的操作，请忽略此邮件。</p>" +
                "</body></html>",
                code
            );

            helper.setText(content, true);
            mailSender.send(message);

            log.info("验证码邮件已发送至：{}", to);
        } catch (MessagingException e) {
            log.error("发送验证码邮件失败", e);
            throw new CustomException(500, "发送验证码失败，请稍后重试");
        }
    }
}
