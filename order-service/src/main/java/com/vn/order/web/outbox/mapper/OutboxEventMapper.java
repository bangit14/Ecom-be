package com.vn.order.web.outbox.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.order.web.outbox.domain.entity.OutboxEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OutboxEventMapper extends BaseMapper<OutboxEvent> {

    @Select("""
        SELECT * FROM ecommerce_order.outbox_events
        WHERE status = 'PENDING' AND retry_count < max_retries
        ORDER BY created_at ASC
        LIMIT #{limit}
    """)
    List<OutboxEvent> findPendingEvents(@Param("limit") int limit);
}
