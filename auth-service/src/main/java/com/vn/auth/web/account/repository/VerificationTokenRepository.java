package com.vn.auth.web.account.repository;

import com.vn.auth.web.account.domain.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByAccountIdAndUsedFalse(UUID accountId);

    boolean existsByAccountIdAndUsedTrue(UUID accountId);
}

