package com.vn.order.web.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.order.web.order.domain.entity.OrderHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderHistoryMapper extends BaseMapper<OrderHistory> {
}
