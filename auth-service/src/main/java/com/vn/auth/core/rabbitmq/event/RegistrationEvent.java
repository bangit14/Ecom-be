package com.vn.auth.core.rabbitmq.event;

import lombok.Builder;

import java.io.Serializable;
import java.util.UUID;

@Builder
public record RegistrationEvent(
    UUID userId,
    String email,
    String verificationToken,
    String fullName
) implements Serializable {
    public static final long serialVersionUID = 1L;

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public String getFullName() {
        return fullName;
    }
}
