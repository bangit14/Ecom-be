package com.vn.report.web.report.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingProductResponse {
    private Long productId;
    private String productName;
    private String productSlug;
    private long totalQuantitySold;
    private BigDecimal totalRevenue;
}
