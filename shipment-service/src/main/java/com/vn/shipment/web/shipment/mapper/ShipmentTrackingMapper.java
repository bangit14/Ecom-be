package com.vn.shipment.web.shipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.shipment.web.shipment.domain.entity.ShipmentTracking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface ShipmentTrackingMapper extends BaseMapper<ShipmentTracking> {

    @Select("SELECT * FROM ecommerce_shipment.shipment_tracking WHERE shipment_id = #{shipmentId} ORDER BY id DESC LIMIT 1")
    Optional<ShipmentTracking> findByShipmentId(@Param("shipmentId") Long shipmentId);

    @Select("SELECT * FROM ecommerce_shipment.shipment_tracking WHERE tracking_number = #{trackingNumber} LIMIT 1")
    Optional<ShipmentTracking> findByTrackingNumber(@Param("trackingNumber") String trackingNumber);
}
