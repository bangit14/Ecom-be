package com.vn.report.web.report.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportResponse {

    private BigDecimal totalRevenue;
    private long totalOrders;
    private List<RevenueByPeriod> revenueByPeriod;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueByPeriod {
        private String period;
        private BigDecimal revenue;
        private long orderCount;
    }
}
