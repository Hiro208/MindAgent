package com.mind.service.impl;

import com.mind.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendEmailAsync(String to, String subject, String content) {
        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            message.setFrom(from);

            mailSender.send(message);

            log.info("Asynchronous email delivered; recipient: {}, Subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Asynchronous email delivery failed; recipient: {}, Subject: {}, Error: {}", to, subject, e.getMessage(), e);
        }
    }
}

