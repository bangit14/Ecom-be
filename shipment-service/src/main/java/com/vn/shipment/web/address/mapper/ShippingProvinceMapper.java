package com.vn.shipment.web.address.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.shipment.web.address.domain.entity.ShippingProvince;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ShippingProvinceMapper extends BaseMapper<ShippingProvince> {

    @Select("SELECT * FROM ecommerce_shipment.shipping_province WHERE provider_id = #{providerId} ORDER BY province_name ASC")
    List<ShippingProvince> findByProviderId(@Param("providerId") Long providerId);

    @Select("""
        SELECT * FROM ecommerce_shipment.shipping_province
        WHERE provider_id = #{providerId} AND external_code = #{externalCode}
        LIMIT 1
    """)
    Optional<ShippingProvince> findByProviderIdAndExternalCode(@Param("providerId") Long providerId, @Param("externalCode") String externalCode);
}
