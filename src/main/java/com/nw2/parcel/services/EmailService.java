package com.nw2.parcel.services;

import com.nw2.parcel.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void send(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new ExternalServiceException("Email sending failed", e);
        }
    }

    // ถ้ายังอยากเก็บไว้
    public void sendResetPassword(String email, String link) {
        send(email, "Reset your password", "Click here: " + link);
    }
}
