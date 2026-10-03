package com.vn.shipment.web.rate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.shipment.web.rate.domain.entity.ShippingRate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ShippingRateMapper extends BaseMapper<ShippingRate> {

    @Select("""
        SELECT * FROM ecommerce_shipment.shipping_rate
        WHERE provider_id = #{providerId}
          AND is_active = true
          AND (service_id = #{serviceId} OR #{serviceId} IS NULL OR service_id IS NULL)
          AND (from_province = #{fromProvince} OR from_province IS NULL)
          AND (to_province = #{toProvince} OR to_province IS NULL)
          AND (weight_to_gram >= #{weightGram} OR weight_to_gram IS NULL)
        ORDER BY 
          CASE WHEN from_province = #{fromProvince} AND to_province = #{toProvince} THEN 1
               WHEN from_province = #{fromProvince} OR to_province = #{toProvince} THEN 2
               ELSE 3 END ASC,
          base_fee ASC
        LIMIT 1
    """)
    Optional<ShippingRate> findMatchingRate(@Param("providerId") Long providerId,
                                            @Param("serviceId") Long serviceId,
                                            @Param("fromProvince") String fromProvince,
                                            @Param("toProvince") String toProvince,
                                            @Param("weightGram") Integer weightGram);

    @Select("""
        SELECT * FROM ecommerce_shipment.shipping_rate
        WHERE is_active = true
          AND (from_province = #{fromProvince} OR from_province IS NULL)
          AND (to_province = #{toProvince} OR to_province IS NULL)
          AND (weight_to_gram >= #{weightGram} OR weight_to_gram IS NULL)
        ORDER BY base_fee ASC
    """)
    List<ShippingRate> findAvailableRates(@Param("fromProvince") String fromProvince,
                                          @Param("toProvince") String toProvince,
                                          @Param("weightGram") Integer weightGram);
}
