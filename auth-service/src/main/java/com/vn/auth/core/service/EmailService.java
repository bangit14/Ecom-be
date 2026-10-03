package com.vn.auth.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationEmail(String to, String fullName, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Xác thực tài khoản E-commerce");

            String emailContent =
                    "Xin chào " + fullName + ",\n\n" +
                            "Mã xác thực email của bạn là: " + otp + "\n\n" +
                            "Mã có hiệu lực trong 15 phút. Vui lòng nhập mã này vào ứng dụng/web để hoàn tất đăng ký.\n\n" +
                            "Không chia sẻ mã này với bất kỳ ai.\n\n" +
                            "Trân trọng,\n" +
                            "E-commerce Team";

            message.setText(emailContent);
            mailSender.send(message);
            log.info("Sent verification email to {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String fullName, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("[E-commerce] Cấp lại mật khẩu tài khoản của bạn");

            String emailContent =
                    "Xin chào " + fullName + ",\n\n" +
                            "Chúng tôi đã nhận được yêu cầu cấp lại mật khẩu cho tài khoản của bạn tại E-commerce.\n\n" +
                            "Mật khẩu mới của bạn là: " + password + "\n\n" +
                            "Vì lý do bảo mật, bạn vui lòng đăng nhập và thay đổi mật khẩu ngay sau khi truy cập.\n\n" +
                            "Trân trọng,\n" +
                            "E-commerce Team";

            message.setText(emailContent);
            mailSender.send(message);
            log.info("Sent password reset email to {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }
}
