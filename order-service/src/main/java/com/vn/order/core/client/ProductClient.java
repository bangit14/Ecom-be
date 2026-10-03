package com.vn.order.core.client;

import com.vn.order.core.exception.AppException;
import com.vn.order.core.exception.ErrorCode;
import com.vn.order.web.order.domain.response.ProductVariantResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j(topic = "ProductClient")
public class ProductClient {

    private final RestClient restClient;
    private final String productServiceUrl;

    public ProductClient(RestClient restClient,
                         @Value("${services.product-service.url:http://localhost:8083}") String productServiceUrl) {
        this.restClient = restClient;
        this.productServiceUrl = productServiceUrl;
    }

    public ProductVariantResponse getVariant(Long variantId) {
        try {
            return restClient.get()
                    .uri(productServiceUrl + "/api/internal/products/variants/{variantId}", variantId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.warn("Variant {} not found in product-service (status: {})", variantId, response.getStatusCode());
                        throw new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND);
                    })
                    .body(ProductVariantResponse.class);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to connect to product-service: {}", e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Không thể kết nối đến product-service");
        }
    }

    public void reserveStock(Long variantId, int quantity) {
        try {
            restClient.post()
                    .uri(productServiceUrl + "/api/internal/products/variants/{variantId}/stock/reserve?quantity={quantity}", variantId, quantity)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.warn("Failed to reserve stock for variant {} (status: {})", variantId, response.getStatusCode());
                        throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
                    })
                    .toBodilessEntity();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error reserving stock for variant {}: {}", variantId, e.getMessage());
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK, "Lỗi trừ tồn kho cho variant " + variantId);
        }
    }

    public void releaseStock(Long variantId, int quantity) {
        try {
            restClient.post()
                    .uri(productServiceUrl + "/api/internal/products/variants/{variantId}/stock/release?quantity={quantity}", variantId, quantity)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        log.warn("Failed to release stock for variant {} (status: {})", variantId, response.getStatusCode());
                    })
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Error releasing stock for variant {}: {}", variantId, e.getMessage());
        }
    }
}
