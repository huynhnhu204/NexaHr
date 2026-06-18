package com.nexahr.service;

public interface EmailService {
    void sendPasswordResetEmail(String email, String token);
    void sendNotificationEmail(String to, String subject, String body);
}
