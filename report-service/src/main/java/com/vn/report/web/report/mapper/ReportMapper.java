package com.vn.report.web.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.report.web.report.domain.entity.OrderReport;
import com.vn.report.web.report.domain.response.RevenueReportResponse;
import com.vn.report.web.report.domain.response.TopSellingProductResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReportMapper extends BaseMapper<OrderReport> {

    @Select("SELECT COUNT(*) FROM ecommerce_order.orders")
    long countOrders();

    @Select("SELECT COUNT(*) FROM ecommerce_order.orders WHERE status = #{status}")
    long countOrdersByStatus(@Param("status") String status);

    @Select("SELECT COALESCE(SUM(final_amount), 0) FROM ecommerce_order.orders WHERE status = #{status}")
    BigDecimal sumRevenueByStatus(@Param("status") String status);

    @Select("""
        SELECT COALESCE(SUM(final_amount), 0) FROM ecommerce_order.orders
        WHERE status = #{status} AND created_at >= #{from} AND created_at < #{to}
    """)
    BigDecimal sumRevenueBetween(@Param("status") String status,
                                 @Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to);

    @Select("SELECT COUNT(*) FROM ecommerce_product.products")
    long countProducts();

    @Select("SELECT COUNT(*) FROM ecommerce_auth.users")
    long countUsers();

    @Select("SELECT COUNT(*) FROM ecommerce_product.reviews")
    long countReviews();

    @Select("""
        SELECT CAST(created_at AS date)::text as period,
               COALESCE(SUM(final_amount), 0) as revenue,
               COUNT(*) as orderCount
        FROM ecommerce_order.orders
        WHERE status = #{status} AND created_at >= #{from} AND created_at < #{to}
        GROUP BY CAST(created_at AS date)
        ORDER BY CAST(created_at AS date)
    """)
    List<RevenueReportResponse.RevenueByPeriod> revenueGroupedByDay(@Param("status") String status,
                                                                   @Param("from") LocalDateTime from,
                                                                   @Param("to") LocalDateTime to);

    @Select("""
        SELECT TO_CHAR(created_at, 'YYYY-MM') as period,
               COALESCE(SUM(final_amount), 0) as revenue,
               COUNT(*) as orderCount
        FROM ecommerce_order.orders
        WHERE status = #{status} AND created_at >= #{from} AND created_at < #{to}
        GROUP BY TO_CHAR(created_at, 'YYYY-MM')
        ORDER BY TO_CHAR(created_at, 'YYYY-MM')
    """)
    List<RevenueReportResponse.RevenueByPeriod> revenueGroupedByMonth(@Param("status") String status,
                                                                     @Param("from") LocalDateTime from,
                                                                     @Param("to") LocalDateTime to);

    @Select("""
        SELECT i.product_id as productId,
               i.product_name as productName,
               p.slug as productSlug,
               COALESCE(SUM(i.quantity), 0) as totalQuantitySold,
               COALESCE(SUM(i.subtotal), 0) as totalRevenue
        FROM ecommerce_order.order_items i
        JOIN ecommerce_order.orders o ON i.order_id = o.id
        LEFT JOIN ecommerce_product.products p ON i.product_id = p.id
        WHERE o.status = #{status}
          AND (#{from}::timestamp IS NULL OR o.created_at >= #{from})
          AND (#{to}::timestamp IS NULL OR o.created_at < #{to})
        GROUP BY i.product_id, i.product_name, p.slug
        ORDER BY totalQuantitySold DESC
        LIMIT #{limit}
    """)
    List<TopSellingProductResponse> findTopSellingProducts(@Param("status") String status,
                                                           @Param("from") LocalDateTime from,
                                                           @Param("to") LocalDateTime to,
                                                           @Param("limit") int limit);
}
