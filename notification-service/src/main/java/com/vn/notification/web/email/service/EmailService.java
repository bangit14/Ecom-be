package com.vn.notification.web.email.service;

public interface EmailService {

    void sendVerificationEmail(String to, String fullName, String verificationToken);
}
