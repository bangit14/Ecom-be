package com.vn.notification.web.email.service.impl;

import com.vn.notification.web.email.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:candyyeukoi10@gmail.com}")
    private String fromEmail;

    @Override
    public void sendVerificationEmail(String to, String fullName, String verificationToken) {
        if (to == null || to.trim().isEmpty()) {
            log.warn("Cannot send verification email: recipient email is null/empty");
            return;
        }

        String recipient = (fullName != null && !fullName.trim().isEmpty()) ? fullName : "Quý khách";

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            try {
                helper.setFrom(fromEmail, "Shobbe");
            } catch (Exception e) {
                helper.setFrom(fromEmail);
            }

            helper.setTo(to);
            helper.setSubject("Mã xác thực tài khoản Shobbe - " + verificationToken);

            String htmlContent;
            try {
                Context context = new Context();
                context.setVariable("fullName", recipient);
                context.setVariable("verificationToken", verificationToken);
                context.setVariable("expiryMinutes", 15);
                htmlContent = templateEngine.process("email/verification-email", context);
            } catch (Exception te) {
                log.warn("Failed to render Thymeleaf template, falling back to basic HTML: {}", te.getMessage());
                htmlContent = buildFallbackHtml(recipient, verificationToken);
            }

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("Sent HTML verification email successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email to " + to + ": " + e.getMessage(), e);
        }
    }

    private String buildFallbackHtml(String fullName, String token) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 10px;">
                <h2 style="color: #ee4d2d; margin-top: 0;">Shobbe - Xác thực tài khoản</h2>
                <p>Xin chào <strong>%s</strong>,</p>
                <p>Mã xác thực đăng ký tài khoản của bạn là:</p>
                <div style="background-color: #fff3f0; border: 2px dashed #ee4d2d; border-radius: 8px; padding: 15px; text-align: center; margin: 20px 0;">
                    <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #ee4d2d;">%s</span>
                </div>
                <p style="color: #64748b; font-size: 13px;">Mã có hiệu lực trong 15 phút. Tuyệt đối không chia sẻ mã này với bất kỳ ai.</p>
                <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;" />
                <p style="font-size: 12px; color: #94a3b8; text-align: center;">© 2026 Shobbe Platform</p>
            </div>
            """.formatted(fullName, token);
    }
}
