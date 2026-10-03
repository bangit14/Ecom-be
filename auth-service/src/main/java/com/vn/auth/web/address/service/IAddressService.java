package com.vn.auth.web.address.service;

import com.vn.auth.web.address.domain.request.AddressCreateRequest;
import com.vn.auth.web.address.domain.request.AddressUpdateRequest;
import com.vn.auth.web.address.domain.response.AddressResponse;

import java.util.List;
import java.util.UUID;

public interface IAddressService {

    List<AddressResponse> getAllAddressesByUserId(UUID userId);

    AddressResponse addAddress(UUID userId, AddressCreateRequest request);

    void updateAddress(UUID id, UUID userId, AddressUpdateRequest request);

    void deleteAddress(UUID userId, UUID id);

    void setDefaultAddress(UUID userId, UUID id);

    AddressResponse getAddressById(UUID id);

    AddressResponse getAddressById(String id);

    AddressResponse getDefaultAddress(UUID userId);

    AddressResponse getDefaultAddress(String userId);
}
