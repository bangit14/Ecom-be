package com.vn.auth.web.store.service;

import com.vn.auth.web.store.domain.request.StoreRegisterRequest;
import com.vn.auth.web.store.domain.response.StoreResponse;

import java.util.List;
import java.util.UUID;

public interface IStoreService {
    StoreResponse getStoreById(UUID id);

    StoreResponse registerStore(StoreRegisterRequest request);

    List<StoreResponse> getAllStores();
}
