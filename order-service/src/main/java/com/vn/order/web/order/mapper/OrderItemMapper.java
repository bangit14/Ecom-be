package com.vn.order.web.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.order.web.order.domain.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
