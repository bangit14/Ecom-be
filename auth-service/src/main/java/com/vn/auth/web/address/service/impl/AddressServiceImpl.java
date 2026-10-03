package com.vn.auth.web.address.service.impl;

import com.vn.auth.core.exception.AppException;
import com.vn.auth.core.exception.ErrorCode;
import com.vn.auth.web.account.repository.AccountRepository;
import com.vn.auth.web.address.domain.entity.Address;
import com.vn.auth.web.address.domain.request.AddressCreateRequest;
import com.vn.auth.web.address.domain.request.AddressUpdateRequest;
import com.vn.auth.web.address.domain.response.AddressResponse;
import com.vn.auth.web.address.repository.AddressRepository;
import com.vn.auth.web.address.service.IAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "Address-Service")
public class AddressServiceImpl implements IAddressService {

    private final AddressRepository addressRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAllAddressesByUserId(UUID userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressResponse addAddress(UUID userId, AddressCreateRequest request) {
        if (!accountRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        boolean isDefault = request.isDefault();
        long count = addressRepository.countByUserId(userId);
        if (count == 0) {
            isDefault = true;
        } else if (isDefault) {
            resetDefaultAddress(userId);
        }

        Address address = Address.builder()
                .userId(userId)
                .recipentName(request.resolveRecipentName())
                .phone(request.getPhone())
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .addressDetail(request.resolveAddressDetail())
                .isDefault(isDefault)
                .build();

        address = addressRepository.save(address);
        log.debug("Address created: id={}, userId={}", address.getId(), userId);

        return mapToAddressResponse(address);
    }

    @Override
    @Transactional
    public void updateAddress(UUID id, UUID userId, AddressUpdateRequest request) {
        Address address = addressRepository.findById(id)
                .filter(a -> a.getUserId().equals(userId))
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        if (request.resolveRecipentName() != null) {
            address.setRecipentName(request.resolveRecipentName());
        }
        if (request.getPhone() != null) {
            address.setPhone(request.getPhone());
        }
        if (request.getProvince() != null) {
            address.setProvince(request.getProvince());
        }
        if (request.getDistrict() != null) {
            address.setDistrict(request.getDistrict());
        }
        if (request.getWard() != null) {
            address.setWard(request.getWard());
        }
        if (request.resolveAddressDetail() != null) {
            address.setAddressDetail(request.resolveAddressDetail());
        }

        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.getIsDefault()) {
            resetDefaultAddress(userId);
            address.setIsDefault(true);
        }

        addressRepository.save(address);
    }

    @Override
    @Transactional
    public void deleteAddress(UUID userId, UUID id) {
        Address address = addressRepository.findById(id)
                .filter(a -> a.getUserId().equals(userId))
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        boolean wasDefault = address.getIsDefault();
        addressRepository.delete(address);

        if (wasDefault) {
            addressRepository.findByUserId(userId).stream().findFirst().ifPresent(next -> {
                next.setIsDefault(true);
                addressRepository.save(next);
            });
        }
    }

    @Override
    @Transactional
    public void setDefaultAddress(UUID userId, UUID id) {
        Address address = addressRepository.findById(id)
                .filter(a -> a.getUserId().equals(userId))
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        resetDefaultAddress(userId);
        address.setIsDefault(true);
        addressRepository.save(address);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(UUID id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        return mapToAddressResponse(address);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(String idStr) {
        try {
            return getAddressById(UUID.fromString(idStr));
        } catch (Exception e) {
            throw new AppException(ErrorCode.ADDRESS_NOT_FOUND);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getDefaultAddress(UUID userId) {
        Address address = addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseGet(() -> addressRepository.findByUserId(userId).stream()
                        .findFirst()
                        .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND)));
        return mapToAddressResponse(address);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getDefaultAddress(String userIdStr) {
        try {
            return getDefaultAddress(UUID.fromString(userIdStr));
        } catch (Exception e) {
            throw new AppException(ErrorCode.ADDRESS_NOT_FOUND);
        }
    }

    private void resetDefaultAddress(UUID userId) {
        addressRepository.findByUserIdAndIsDefaultTrue(userId).ifPresent(oldDefault -> {
            oldDefault.setIsDefault(false);
            addressRepository.save(oldDefault);
        });
    }

    private AddressResponse mapToAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .userId(address.getUserId())
                .recipentName(address.getRecipentName())
                .fullName(address.getRecipentName())
                .phone(address.getPhone())
                .province(address.getProvince())
                .district(address.getDistrict())
                .ward(address.getWard())
                .addressDetail(address.getAddressDetail())
                .addressLine(address.getAddressDetail())
                .isDefault(Boolean.TRUE.equals(address.getIsDefault()))
                .build();
    }
}
