package com.vn.shipment.web.shipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.shipment.web.shipment.domain.entity.ShipmentEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShipmentEventMapper extends BaseMapper<ShipmentEvent> {

    @Select("SELECT * FROM ecommerce_shipment.shipment_event WHERE shipment_id = #{shipmentId} ORDER BY event_time DESC, id DESC")
    List<ShipmentEvent> findByShipmentId(@Param("shipmentId") Long shipmentId);
}
