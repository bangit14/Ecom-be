package com.vn.order.web.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.order.web.order.domain.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT COALESCE(SUM(final_amount), 0) FROM ecommerce_order.orders WHERE status = #{status}")
    BigDecimal sumRevenueByStatus(@Param("status") String status);

    @Select("""
        SELECT COALESCE(SUM(final_amount), 0) FROM ecommerce_order.orders
        WHERE status = #{status} AND created_at >= #{from} AND created_at < #{to}
    """)
    BigDecimal sumRevenueBetween(@Param("status") String status,
                                 @Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to);

    @Select("""
        SELECT CAST(created_at AS date) as day, SUM(final_amount) as revenue, COUNT(*) as count
        FROM ecommerce_order.orders
        WHERE status = #{status} AND created_at >= #{from} AND created_at < #{to}
        GROUP BY CAST(created_at AS date)
        ORDER BY CAST(created_at AS date)
    """)
    List<Map<String, Object>> revenueGroupedByDay(@Param("status") String status,
                                                  @Param("from") LocalDateTime from,
                                                  @Param("to") LocalDateTime to);
}
