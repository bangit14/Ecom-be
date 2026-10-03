package com.vn.shipment.web.ghn.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.shipment.core.enums.GhnPrintSize;
import com.vn.shipment.core.exception.AppException;
import com.vn.shipment.core.exception.ErrorCode;
import com.vn.shipment.web.ghn.domain.dto.*;
import com.vn.shipment.web.ghn.domain.request.*;
import com.vn.shipment.web.ghn.domain.response.GhnCreateOrderResponse;
import com.vn.shipment.web.ghn.domain.response.GhnPrintOrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class GhnApiClient {

    @Value("${ghn.url:https://dev-online-gateway.ghn.vn}")
    private String defaultBaseUrl;

    @Value("${ghn.token-dev:ec9a4822-af6e-11f1-a973-aee5264794df}")
    private String defaultToken;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GhnApiClient(RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    private String resolveToken(String customToken) {
        return StringUtils.hasText(customToken) ? customToken : defaultToken;
    }

    private String resolveBaseUrl(String customUrl) {
        return StringUtils.hasText(customUrl) ? customUrl : defaultBaseUrl;
    }

    // =========================================================================
    // MASTER DATA APIS
    // =========================================================================

    public List<GhnProvinceDto> getProvinces(String customToken, Long shopId) {
        String url = resolveBaseUrl(null) + "/shiip/public-api/master-data/province";
        log.info("GHN API Request: GET {}", url);

        try {
            GhnApiResponse<List<GhnProvinceDto>> response = restClient.get()
                    .uri(url)
                    .header("Token", resolveToken(customToken))
                    .headers(h -> { if (shopId != null) h.set("ShopId", String.valueOf(shopId)); })
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<GhnApiResponse<List<GhnProvinceDto>>>() {});

            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
            log.warn("GHN getProvinces response error: {}", response);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error calling GHN getProvinces: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.CARRIER_API_ERROR, "Lỗi lấy danh sách tỉnh/thành từ GHN: " + e.getMessage());
        }
    }

    public List<GhnDistrictDto> getDistricts(Integer provinceId, String customToken, Long shopId) {
        String url = resolveBaseUrl(null) + "/shiip/public-api/master-data/district?province_id=" + provinceId;
        log.info("GHN API Request: GET {}", url);

        try {
            GhnApiResponse<List<GhnDistrictDto>> response = restClient.get()
                    .uri(url)
                    .header("Token", resolveToken(customToken))
                    .headers(h -> { if (shopId != null) h.set("ShopId", String.valueOf(shopId)); })
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<GhnApiResponse<List<GhnDistrictDto>>>() {});

            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
            log.warn("GHN getDistricts response error: {}", response);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error calling GHN getDistricts: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.CARRIER_API_ERROR, "Lỗi lấy danh sách quận/huyện từ GHN: " + e.getMessage());
        }
    }

    public List<GhnWardDto> getWards(Integer districtId, String customToken, Long shopId) {
        String url = resolveBaseUrl(null) + "/shiip/public-api/master-data/ward?district_id=" + districtId;
        log.info("GHN API Request: GET {}", url);

        try {
            GhnApiResponse<List<GhnWardDto>> response = restClient.get()
                    .uri(url)
                    .header("Token", resolveToken(customToken))
                    .headers(h -> { if (shopId != null) h.set("ShopId", String.valueOf(shopId)); })
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<GhnApiResponse<List<GhnWardDto>>>() {});

            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
            log.warn("GHN getWards response error: {}", response);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error calling GHN getWards: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.CARRIER_API_ERROR, "Lỗi lấy danh sách phường/xã từ GHN: " + e.getMessage());
        }
    }

    public List<GhnServicePackageDto> getAvailableServices(GhnAvailableServicesRequest request, String customToken, Long shopId) {
        String url = resolveBaseUrl(null) + "/shiip/public-api/v2/shipping-order/available-services";
        log.info("GHN API Request: POST {} with body {}", url, request);

        try {
            GhnApiResponse<List<GhnServicePackageDto>> response = restClient.post()
                    .uri(url)
                    .header("Token", resolveToken(customToken))
                    .headers(h -> {
                        Long sid = request.getShopId() != null ? request.getShopId() : shopId;
                        if (sid != null) h.set("ShopId", String.valueOf(sid));
                    })
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<GhnApiResponse<List<GhnServicePackageDto>>>() {});

            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
            log.warn("GHN getAvailableServices response: {}", response);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error calling GHN available-services: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.CARRIER_API_ERROR, "Lỗi lấy danh sách dịch vụ GHN khả dụng: " + e.getMessage());
        }
    }

    public List<GhnPickShiftDto> getPickShifts(String customToken) {
        String url = resolveBaseUrl(null) + "/shiip/public-api/v2/shift/date";
        log.info("GHN API Request: GET {}", url);

        try {
            GhnApiResponse<List<GhnPickShiftDto>> response = restClient.get()
                    .uri(url)
                    .header("Token", resolveToken(customToken))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<GhnApiResponse<List<GhnPickShiftDto>>>() {});

            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error calling GHN pick-shifts: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.CARRIER_API_ERROR, "Lỗi lấy ca lấy hàng GHN: " + e.getMessage());
        }
    }

    public List<GhnStationDto> getStations(Integer districtId, String customToken) {
        String url = resolveBaseUrl(null) + "/shiip/public-api/v2/station/get";
        log.info("GHN API Request: POST {} for district {}", url, districtId);

        try {
            Map<String, Object> body = new HashMap<>();
            if (districtId != null) body.put("district_id", districtId);

            GhnApiResponse<List<GhnStationDto>> response = restClient.post()
                    .uri(url)
                    .header("Token", resolveToken(customToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<GhnApiResponse<List<GhnStationDto>>>() {});

            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error calling GHN getStations: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // =========================================================================
    // SHIPPING ORDER APIS
    // =========================================================================

    public GhnCreateOrderResponse createOrder(GhnCreateOrderRequest request, String customToken, Long shopId) {
        String url = resolveBaseUrl(null) + "/shiip/public-api/v2/shipping-order/create";
        log.info("GHN API Request: POST {} with clientOrderCode: {}", url, request.getClientOrderCode());

        try {
            GhnApiResponse<GhnCreateOrderResponse> response = restClient.post()
                    .uri(url)
                    .header("Token", resolveToken(customToken))
                    .headers(h -> { if (shopId != null) h.set("ShopId", String.valueOf(shopId)); })
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<GhnApiResponse<GhnCreateOrderResponse>>() {});

            if (response != null && response.isSuccess() && response.getData() != null) {
                log.info("GHN order created successfully! OrderCode: {}", response.getData().getOrderCode());
                return response.getData();
            }

            String msg = response != null ? (response.getMessageDisplay() != null ? response.getMessageDisplay() : response.getMessage()) : "Không nhận được phản hồi";
            throw new AppException(ErrorCode.CARRIER_API_ERROR, "Tạo đơn GHN thất bại: " + msg);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating GHN shipping order: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.CARRIER_API_ERROR, "Lỗi gọi API tạo đơn GHN: " + e.getMessage());
        }
    }

    public GhnFeeDetailDto calculateFee(GhnCalculateFeeRequest request, String customToken, Long shopId) {
        String url = resolveBaseUrl(null) + "/shiip/public-api/v2/shipping-order/fee";
        log.info("GHN API Request: POST {}", url);

        try {
            GhnApiResponse<GhnFeeDetailDto> response = restClient.post()
                    .uri(url)
                    .header("Token", resolveToken(customToken))
                    .headers(h -> { if (shopId != null) h.set("ShopId", String.valueOf(shopId)); })
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<GhnApiResponse<GhnFeeDetailDto>>() {});

            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }

            String msg = response != null ? response.getMessage() : "Lỗi tính phí";
            throw new AppException(ErrorCode.CARRIER_API_ERROR, "Tính phí GHN thất bại: " + msg);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calculating GHN fee: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.CARRIER_API_ERROR, "Lỗi gọi API tính phí GHN: " + e.getMessage());
        }
    }

    public LocalDateTime calculateLeadtime(GhnLeadtimeRequest request, String customToken, Long shopId) {
        String url = resolveBaseUrl(null) + "/shiip/public-api/v2/shipping-order/leadtime";
        log.info("GHN API Request: POST {}", url);

        try {
            GhnApiResponse<JsonNode> response = restClient.post()
                    .uri(url)
                    .header("Token", resolveToken(customToken))
                    .headers(h -> { if (shopId != null) h.set("ShopId", String.valueOf(shopId)); })
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<GhnApiResponse<JsonNode>>() {});

            if (response != null && response.isSuccess() && response.getData() != null) {
                long leadtimeSeconds = response.getData().path("leadtime").asLong(0);
                if (leadtimeSeconds > 0) {
                    return LocalDateTime.ofInstant(Instant.ofEpochSecond(leadtimeSeconds), ZoneId.systemDefault());
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error calculating GHN leadtime: {}", e.getMessage(), e);
            return null;
        }
    }

    public void cancelOrder(GhnCancelOrderRequest request, String customToken, Long shopId) {
        String url = resolveBaseUrl(null) + "/shiip/public-api/v2/switch-status/cancel";
        log.info("GHN API Request: POST {} cancel orders: {}", url, request.getOrderCodes());

        try {
            GhnApiResponse<Object> response = restClient.post()
                    .uri(url)
                    .header("Token", resolveToken(customToken))
                    .headers(h -> { if (shopId != null) h.set("ShopId", String.valueOf(shopId)); })
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<GhnApiResponse<Object>>() {});

            if (response != null && response.isSuccess()) {
                log.info("GHN orders cancelled successfully: {}", request.getOrderCodes());
                return;
            }

            String msg = response != null ? response.getMessage() : "Lỗi hủy đơn";
            throw new AppException(ErrorCode.CARRIER_API_ERROR, "Hủy đơn GHN thất bại: " + msg);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error cancelling GHN order: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.CARRIER_API_ERROR, "Lỗi gọi API hủy đơn GHN: " + e.getMessage());
        }
    }

    public GhnPrintOrderResponse generatePrintToken(GhnPrintOrderRequest request, String customToken, Long shopId) {
        String url = resolveBaseUrl(null) + "/shiip/public-api/v2/a5/gen-token";
        log.info("GHN API Request: POST {} gen print token for: {}", url, request.getOrderCodes());

        try {
            Map<String, Object> body = Map.of("order_codes", request.getOrderCodes());

            GhnApiResponse<JsonNode> response = restClient.post()
                    .uri(url)
                    .header("Token", resolveToken(customToken))
                    .headers(h -> { if (shopId != null) h.set("ShopId", String.valueOf(shopId)); })
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<GhnApiResponse<JsonNode>>() {});

            if (response != null && response.isSuccess() && response.getData() != null) {
                String token = response.getData().path("token").asText();
                GhnPrintSize size = request.getPrintSize() != null ? request.getPrintSize() : GhnPrintSize.A5;
                String printUrl = resolveBaseUrl(null) + "/a5/public-api/" + size.getApiPath() + "?token=" + token;

                return GhnPrintOrderResponse.builder()
                        .token(token)
                        .printUrl(printUrl)
                        .orderCodes(request.getOrderCodes())
                        .build();
            }

            String msg = response != null ? response.getMessage() : "Lỗi sinh token in";
            throw new AppException(ErrorCode.CARRIER_API_ERROR, "Sinh token in GHN thất bại: " + msg);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generating GHN print token: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.CARRIER_API_ERROR, "Lỗi sinh token in GHN: " + e.getMessage());
        }
    }
}
