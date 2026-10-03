package com.vn.report.web.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vn.report.web.report.domain.entity.OrderReport;
import com.vn.report.web.report.domain.response.DashboardResponse;
import com.vn.report.web.report.domain.response.RevenueReportResponse;
import com.vn.report.web.report.domain.response.TopSellingProductResponse;

import java.time.LocalDate;
import java.util.List;

public interface IReportService extends IService<OrderReport> {

    DashboardResponse getDashboard();

    RevenueReportResponse getRevenueReport(LocalDate from, LocalDate to, String groupBy);

    List<TopSellingProductResponse> getTopSellingProducts(LocalDate from, LocalDate to, int limit);
}
