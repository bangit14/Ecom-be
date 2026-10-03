package com.vn.shipment.web.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.shipment.web.provider.domain.entity.ShippingProviderService;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ShippingProviderServiceMapper extends BaseMapper<ShippingProviderService> {

    @Select("SELECT * FROM ecommerce_shipment.shipping_provider_service WHERE provider_id = #{providerId} AND is_active = true")
    List<ShippingProviderService> findByProviderId(@Param("providerId") Long providerId);

    @Select("""
        SELECT * FROM ecommerce_shipment.shipping_provider_service
        WHERE provider_id = #{providerId} AND service_code = #{serviceCode} AND is_active = true
        LIMIT 1
    """)
    Optional<ShippingProviderService> findByProviderIdAndServiceCode(@Param("providerId") Long providerId, @Param("serviceCode") String serviceCode);
}
