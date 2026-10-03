package com.vn.order.core.client;

import com.vn.order.core.exception.AppException;
import com.vn.order.core.exception.ErrorCode;
import com.vn.order.web.order.domain.response.AddressResponse;
import com.vn.order.web.order.domain.response.UserSummaryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j(topic = "AuthClient")
public class AuthClient {

    private final RestClient restClient;
    private final String authServiceUrl;

    public AuthClient(RestClient restClient,
                      @Value("${services.auth-service.url:http://localhost:8084}") String authServiceUrl) {
        this.restClient = restClient;
        this.authServiceUrl = authServiceUrl;
    }

    public UserSummaryResponse getUser(Long userId) {
        try {
            return restClient.get()
                    .uri(authServiceUrl + "/api/internal/users/{userId}", userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.warn("User {} not found in auth-service (status: {})", userId, response.getStatusCode());
                    })
                    .body(UserSummaryResponse.class);
        } catch (Exception e) {
            log.warn("Could not retrieve user info from auth-service for userId {}: {}", userId, e.getMessage());
            return null;
        }
    }

    public AddressResponse getAddress(Long addressId) {
        try {
            return restClient.get()
                    .uri(authServiceUrl + "/api/internal/addresses/{addressId}", addressId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.warn("Address {} not found in auth-service (status: {})", addressId, response.getStatusCode());
                        throw new AppException(ErrorCode.ADDRESS_NOT_FOUND);
                    })
                    .body(AddressResponse.class);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to connect to auth-service: {}", e.getMessage());
            throw new AppException(ErrorCode.ADDRESS_NOT_FOUND, "Không thể lấy thông tin địa chỉ từ auth-service");
        }
    }

    public AddressResponse getDefaultAddress(Long userId) {
        try {
            return restClient.get()
                    .uri(authServiceUrl + "/api/internal/addresses/default?userId={userId}", userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.warn("Default address for user {} not found in auth-service (status: {})", userId, response.getStatusCode());
                        throw new AppException(ErrorCode.ADDRESS_NOT_FOUND);
                    })
                    .body(AddressResponse.class);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to connect to auth-service: {}", e.getMessage());
            throw new AppException(ErrorCode.ADDRESS_NOT_FOUND, "Không thể lấy địa chỉ mặc định từ auth-service");
        }
    }
}
