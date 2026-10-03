package com.vn.auth.web.store;

import com.vn.auth.web.store.domain.request.StoreRegisterRequest;
import com.vn.auth.web.store.domain.response.StoreResponse;
import com.vn.auth.web.store.service.IStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final IStoreService storeService;

    @PostMapping("/register")
    public ResponseEntity<StoreResponse> registerStore(@RequestBody StoreRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.registerStore(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getStoreById(@PathVariable UUID id) {
        return ResponseEntity.ok(storeService.getStoreById(id));
    }

    @GetMapping
    public ResponseEntity<List<StoreResponse>> getAllStores() {
        return ResponseEntity.ok(storeService.getAllStores());
    }
}
