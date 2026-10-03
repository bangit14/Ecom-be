package com.vn.auth.web.account.service;

import com.vn.auth.web.account.domain.entity.Account;
import com.vn.auth.web.account.domain.entity.VerificationToken;

import java.util.UUID;

public interface IVerificationTokenService {
    String createVerificationToken(Account account);
    String createVerificationToken(UUID accountId);
    VerificationToken validateAndGetToken(String token);
    void markTokenAsUsed(VerificationToken token);
    void deleteVerificationToken(VerificationToken token);
}
