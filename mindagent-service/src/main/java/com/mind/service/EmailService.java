package com.mind.service;

public interface EmailService {

    void sendEmailAsync(String to, String subject, String content);
}
