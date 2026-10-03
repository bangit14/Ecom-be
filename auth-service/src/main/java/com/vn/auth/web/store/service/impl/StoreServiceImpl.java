package com.vn.auth.web.store.service.impl;

import com.vn.auth.core.enums.AccountTypeEnum;
import com.vn.auth.core.enums.RoleAccountEnum;
import com.vn.auth.core.exception.AppException;
import com.vn.auth.core.exception.ErrorCode;
import com.vn.auth.web.account.domain.entity.Account;
import com.vn.auth.web.account.repository.AccountRepository;
import com.vn.auth.web.store.domain.entity.Store;
import com.vn.auth.web.store.domain.request.StoreRegisterRequest;
import com.vn.auth.web.store.domain.response.StoreResponse;
import com.vn.auth.web.store.repository.StoreRepository;
import com.vn.auth.web.store.service.IStoreService;
import com.vn.auth.web.user.domain.entity.UserProfile;
import com.vn.auth.web.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "Store-Service")
public class StoreServiceImpl implements IStoreService {

    private final StoreRepository storeRepository;
    private final AccountRepository accountRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public StoreResponse registerStore(StoreRegisterRequest request) {
        if (request.getTaxNumber() != null && !request.getTaxNumber().trim().isEmpty()) {
            if (storeRepository.existsByTaxNumber(request.getTaxNumber().trim())) {
                throw new AppException(ErrorCode.STORE_ALREADY_EXISTS, "Mã số thuế này đã được đăng ký cửa hàng");
            }
        }

        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS, "Tên đăng nhập đã tồn tại");
        }

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email đã tồn tại");
        }

        Store store = Store.builder()
                .name(request.getStoreName())
                .taxNumber(request.getTaxNumber())
                .city(request.getCity())
                .description(request.getDescription())
                .logo(request.getLogo())
                .banner(request.getBanner())
                .status("ACTIVE")
                .build();

        store = storeRepository.save(store);

        Account account = Account.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .type(AccountTypeEnum.DEFAULT.getValue())
                .role(RoleAccountEnum.STORE_OWNER.getValue())
                .storeId(store.getId())
                .build();

        account = accountRepository.save(account);

        UserProfile profile = UserProfile.builder()
                .account(account)
                .name(request.getFullName() != null ? request.getFullName() : request.getUsername())
                .phone(request.getPhone())
                .build();

        userProfileRepository.save(profile);

        log.info("Registered new store: id={}, name={}, owner={}", store.getId(), store.getName(), account.getUsername());

        return mapToResponse(store);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreResponse getStoreById(UUID id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        return mapToResponse(store);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreResponse> getAllStores() {
        return storeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private StoreResponse mapToResponse(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .taxNumber(store.getTaxNumber())
                .city(store.getCity())
                .description(store.getDescription())
                .logo(store.getLogo())
                .banner(store.getBanner())
                .status(store.getStatus())
                .createdAt(store.getCreatedAt())
                .build();
    }
}
