package com.vn.auth.core.service;

import com.vn.auth.core.security.CustomUserDetails;
import com.vn.auth.web.account.domain.entity.Account;
import com.vn.auth.web.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Account " + username + " not found"));

        String fullName = account.getProfile() != null ? account.getProfile().getName() : account.getUsername();
        String phone = account.getProfile() != null ? account.getProfile().getPhone() : null;

        return CustomUserDetails.builder()
                .id(account.getId())
                .storeId(account.getStoreId())
                .username(account.getUsername())
                .email(account.getEmail())
                .fullName(fullName)
                .phone(phone)
                .password(account.getPasswordHash())
                .role(account.getRole())
                .type(account.getType())
                .roles(List.of(account.getRole()))
                .active(true)
                .build();
    }
}
