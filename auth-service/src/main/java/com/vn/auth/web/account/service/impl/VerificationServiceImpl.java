package com.vn.auth.web.account.service.impl;

import com.vn.auth.core.exception.AppException;
import com.vn.auth.core.exception.ErrorCode;
import com.vn.auth.web.account.domain.entity.Account;
import com.vn.auth.web.account.domain.entity.VerificationToken;
import com.vn.auth.web.account.repository.VerificationTokenRepository;
import com.vn.auth.web.account.service.IVerificationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements IVerificationTokenService {

    private final VerificationTokenRepository tokenRepository;

    @Value("${app.email.verification-expiry-minutes:15}")
    private int expiryMinutes = 15;

    @Override
    @Transactional
    public String createVerificationToken(Account account) {
        return createVerificationToken(account.getId());
    }

    @Override
    @Transactional
    public String createVerificationToken(UUID accountId) {
        tokenRepository.findByAccountIdAndUsedFalse(accountId).ifPresent(t -> {
            t.setUsed(true);
            tokenRepository.save(t);
        });

        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .accountId(accountId)
                .expiryDate(LocalDateTime.now().plusMinutes(expiryMinutes))
                .build();

        tokenRepository.save(verificationToken);

        return token;
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationToken validateAndGetToken(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        if (verificationToken.isUsed()) {
            throw new AppException(ErrorCode.TOKEN_USED);
        }

        return verificationToken;
    }

    @Override
    @Transactional
    public void markTokenAsUsed(VerificationToken token) {
        token.setUsed(true);
        tokenRepository.save(token);
    }

    @Override
    @Transactional
    public void deleteVerificationToken(VerificationToken token) {
        tokenRepository.delete(token);
    }
}
