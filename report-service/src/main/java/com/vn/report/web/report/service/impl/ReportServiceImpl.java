package com.vn.report.web.report.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vn.report.web.report.domain.entity.OrderReport;
import com.vn.report.web.report.domain.response.DashboardResponse;
import com.vn.report.web.report.domain.response.RevenueReportResponse;
import com.vn.report.web.report.domain.response.TopSellingProductResponse;
import com.vn.report.web.report.mapper.ReportMapper;
import com.vn.report.web.report.service.IReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "Report-Service")
public class ReportServiceImpl extends ServiceImpl<ReportMapper, OrderReport> implements IReportService {

    private static final String STATUS_DELIVERED = "DELIVERED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CANCELLED = "CANCELLED";

    @Override
    public DashboardResponse getDashboard() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = monthStart.plusMonths(1);

        BigDecimal totalRevenue = orZero(baseMapper.sumRevenueByStatus(STATUS_DELIVERED));
        BigDecimal revenueToday = orZero(baseMapper.sumRevenueBetween(STATUS_DELIVERED, todayStart, todayEnd));
        BigDecimal revenueMonth = orZero(baseMapper.sumRevenueBetween(STATUS_DELIVERED, monthStart, monthEnd));

        return DashboardResponse.builder()
                .totalOrders(baseMapper.countOrders())
                .pendingOrders(baseMapper.countOrdersByStatus(STATUS_PENDING))
                .completedOrders(baseMapper.countOrdersByStatus(STATUS_DELIVERED))
                .cancelledOrders(baseMapper.countOrdersByStatus(STATUS_CANCELLED))
                .totalRevenue(totalRevenue)
                .revenueToday(revenueToday)
                .revenueThisMonth(revenueMonth)
                .totalProducts(baseMapper.countProducts())
                .totalUsers(baseMapper.countUsers())
                .totalReviews(baseMapper.countReviews())
                .build();
    }

    @Override
    public RevenueReportResponse getRevenueReport(LocalDate from, LocalDate to, String groupBy) {
        LocalDateTime fromDt = (from != null ? from : LocalDate.now().minusMonths(1)).atStartOfDay();
        LocalDateTime toDt = (to != null ? to.plusDays(1) : LocalDate.now().plusDays(1)).atStartOfDay();

        List<RevenueReportResponse.RevenueByPeriod> periods = "month".equalsIgnoreCase(groupBy)
                ? baseMapper.revenueGroupedByMonth(STATUS_DELIVERED, fromDt, toDt)
                : baseMapper.revenueGroupedByDay(STATUS_DELIVERED, fromDt, toDt);

        if (periods == null) {
            periods = List.of();
        }

        BigDecimal total = periods.stream()
                .map(RevenueReportResponse.RevenueByPeriod::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = periods.stream()
                .mapToLong(RevenueReportResponse.RevenueByPeriod::getOrderCount)
                .sum();

        return RevenueReportResponse.builder()
                .totalRevenue(total)
                .totalOrders(totalOrders)
                .revenueByPeriod(periods)
                .build();
    }

    @Override
    public List<TopSellingProductResponse> getTopSellingProducts(LocalDate from, LocalDate to, int limit) {
        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt = to != null ? to.plusDays(1).atStartOfDay() : null;
        int effectiveLimit = limit > 0 ? limit : 10;

        List<TopSellingProductResponse> results = baseMapper.findTopSellingProducts(STATUS_DELIVERED, fromDt, toDt, effectiveLimit);
        return results != null ? results : List.of();
    }

    private BigDecimal orZero(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
