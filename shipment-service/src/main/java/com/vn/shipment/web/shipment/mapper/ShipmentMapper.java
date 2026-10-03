package com.vn.shipment.web.shipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.shipment.web.shipment.domain.entity.Shipment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface ShipmentMapper extends BaseMapper<Shipment> {

    @Select("SELECT * FROM ecommerce_shipment.shipment WHERE shipment_code = #{shipmentCode} LIMIT 1")
    Optional<Shipment> findByShipmentCode(@Param("shipmentCode") String shipmentCode);

    @Select("SELECT * FROM ecommerce_shipment.shipment WHERE order_id = #{orderId} ORDER BY id DESC LIMIT 1")
    Optional<Shipment> findByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT * FROM ecommerce_shipment.shipment WHERE external_order_code = #{externalCode} LIMIT 1")
    Optional<Shipment> findByExternalOrderCode(@Param("externalCode") String externalCode);
}
