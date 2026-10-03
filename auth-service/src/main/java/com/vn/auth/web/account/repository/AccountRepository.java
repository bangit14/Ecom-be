package com.vn.auth.web.account.repository;

import com.vn.auth.web.account.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmail(String email);

    @Query("SELECT a FROM Account a WHERE a.username = :login OR a.email = :login")
    Optional<Account> findByUsernameOrEmail(@Param("login") String login);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<Account> findByStoreId(UUID storeId);

    long countByType(String type);
}
