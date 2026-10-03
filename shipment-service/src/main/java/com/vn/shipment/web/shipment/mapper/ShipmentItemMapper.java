package com.vn.shipment.web.shipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.shipment.web.shipment.domain.entity.ShipmentItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShipmentItemMapper extends BaseMapper<ShipmentItem> {

    @Select("SELECT * FROM ecommerce_shipment.shipment_item WHERE shipment_id = #{shipmentId}")
    List<ShipmentItem> findByShipmentId(@Param("shipmentId") Long shipmentId);
}
