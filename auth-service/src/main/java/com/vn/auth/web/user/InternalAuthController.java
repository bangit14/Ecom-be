package com.vn.auth.web.user;

import com.vn.auth.web.address.domain.response.AddressResponse;
import com.vn.auth.web.address.service.IAddressService;
import com.vn.auth.web.user.domain.response.UserSummaryResponse;
import com.vn.auth.web.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalAuthController {

    private final IUserService userService;
    private final IAddressService addressService;

    @GetMapping("/users/{id}")
    public ResponseEntity<UserSummaryResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserSummary(id));
    }

    @GetMapping("/users/count")
    public ResponseEntity<Map<String, Long>> getUserCount() {
        return ResponseEntity.ok(Map.of("count", userService.countUsers()));
    }

    @GetMapping("/addresses/{id}")
    public ResponseEntity<AddressResponse> getAddressById(@PathVariable String id) {
        return ResponseEntity.ok(addressService.getAddressById(id));
    }

    @GetMapping("/addresses/default")
    public ResponseEntity<AddressResponse> getDefaultAddress(@RequestParam String userId) {
        return ResponseEntity.ok(addressService.getDefaultAddress(userId));
    }
}
