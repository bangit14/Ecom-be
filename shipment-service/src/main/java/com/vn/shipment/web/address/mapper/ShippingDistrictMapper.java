package com.vn.shipment.web.address.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.shipment.web.address.domain.entity.ShippingDistrict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ShippingDistrictMapper extends BaseMapper<ShippingDistrict> {

    @Select("SELECT * FROM ecommerce_shipment.shipping_district WHERE province_ref_id = #{provinceRefId} ORDER BY district_name ASC")
    List<ShippingDistrict> findByProvinceRefId(@Param("provinceRefId") Long provinceRefId);

    @Select("""
        SELECT * FROM ecommerce_shipment.shipping_district
        WHERE provider_id = #{providerId} AND external_code = #{externalCode}
        LIMIT 1
    """)
    Optional<ShippingDistrict> findByProviderIdAndExternalCode(@Param("providerId") Long providerId, @Param("externalCode") String externalCode);
}
