package com.vn.auth.web.address;

import com.vn.auth.core.security.SecurityUtils;
import com.vn.auth.web.address.domain.request.AddressCreateRequest;
import com.vn.auth.web.address.domain.request.AddressUpdateRequest;
import com.vn.auth.web.address.domain.response.AddressResponse;
import com.vn.auth.web.address.service.IAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Slf4j(topic = "Address-Controller")
public class AddressController {

    private final IAddressService addressService;

    @GetMapping({"", "/list"})
    public ResponseEntity<List<AddressResponse>> getAllAddresses() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(addressService.getAllAddressesByUserId(userId));
    }

    @PostMapping({"", "/add"})
    public ResponseEntity<AddressResponse> addAddress(@RequestBody AddressCreateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        AddressResponse created = addressService.addAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping({"/{addressId}", "/update/{addressId}"})
    public ResponseEntity<Void> updateAddress(@PathVariable UUID addressId, @RequestBody AddressUpdateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        addressService.updateAddress(addressId, userId, request);
        return ResponseEntity.accepted().build();
    }

    @PutMapping({"/{addressId}/default", "/update/{addressId}/default"})
    public ResponseEntity<Void> setDefaultAddress(@PathVariable UUID addressId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        addressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping({"/{addressId}", "/delete/{addressId}"})
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID addressId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }
}
