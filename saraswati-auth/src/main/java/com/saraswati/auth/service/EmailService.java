package com.saraswati.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Async
    public void sendPasswordResetEmail(String toEmail, String token, String username) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Saraswati Coaching - Password Reset Request");
        message.setText(
            "Dear " + username + ",\n\n" +
            "You requested a password reset. Use the link below (valid for 15 minutes):\n\n" +
            resetLink + "\n\n" +
            "If you did not request this, you can safely ignore this email.\n\n" +
            "Regards,\nSaraswati Coaching Team"
        );

        try {
            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Welcome to Saraswati Coaching!");
        message.setText(
            "Dear " + username + ",\n\n" +
            "Welcome! Your Saraswati Coaching account has been created successfully.\n\n" +
            "Regards,\nSaraswati Coaching Team"
        );

        try {
            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }
}
