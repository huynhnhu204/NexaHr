package com.nexahr.service.impl;

import com.nexahr.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:noreply@nexahr.com}")
    private String mailFrom;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void sendPasswordResetEmail(String email, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        String subject = "NexaHR - Đặt lại mật khẩu";
        String body = "Bạn đã yêu cầu đặt lại mật khẩu. Nhấn vào liên kết sau để tiếp tục:\n\n" + resetUrl
                + "\n\nLiên kết có hiệu lực trong 1 giờ.";

        if (!mailEnabled) {
            log.info("[MAIL DISABLED] Password reset for {}: {}", email, resetUrl);
            return;
        }

        sendNotificationEmail(email, subject, body);
    }

    @Override
    public void sendNotificationEmail(String to, String subject, String body) {
        if (!mailEnabled) {
            log.info("[MAIL DISABLED] To: {}, Subject: {}, Body: {}", to, subject, body);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("Email sent to {}", to);
    }
}
