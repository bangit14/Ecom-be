package com.vn.shipment.web.address.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.shipment.web.address.domain.entity.ShippingWard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ShippingWardMapper extends BaseMapper<ShippingWard> {

    @Select("SELECT * FROM ecommerce_shipment.shipping_ward WHERE district_ref_id = #{districtRefId} ORDER BY ward_name ASC")
    List<ShippingWard> findByDistrictRefId(@Param("districtRefId") Long districtRefId);

    @Select("""
        SELECT * FROM ecommerce_shipment.shipping_ward
        WHERE provider_id = #{providerId} AND external_code = #{externalCode}
        LIMIT 1
    """)
    Optional<ShippingWard> findByProviderIdAndExternalCode(@Param("providerId") Long providerId, @Param("externalCode") String externalCode);
}
