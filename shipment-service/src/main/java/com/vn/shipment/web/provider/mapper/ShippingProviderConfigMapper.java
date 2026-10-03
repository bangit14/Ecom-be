package com.vn.shipment.web.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.shipment.web.provider.domain.entity.ShippingProviderConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface ShippingProviderConfigMapper extends BaseMapper<ShippingProviderConfig> {

    @Select("""
        SELECT * FROM ecommerce_shipment.shipping_provider_config
        WHERE provider_id = #{providerId} AND is_default = true AND is_active = true
        LIMIT 1
    """)
    Optional<ShippingProviderConfig> findDefaultConfigByProviderId(@Param("providerId") Long providerId);

    @Select("""
        SELECT c.* FROM ecommerce_shipment.shipping_provider_config c
        JOIN ecommerce_shipment.shipping_provider p ON c.provider_id = p.id
        WHERE p.code = #{providerCode} AND c.is_default = true AND c.is_active = true
        LIMIT 1
    """)
    Optional<ShippingProviderConfig> findDefaultConfigByProviderCode(@Param("providerCode") String providerCode);

    @Select("""
        SELECT * FROM ecommerce_shipment.shipping_provider_config
        WHERE tenant_id = #{tenantId} AND provider_id = #{providerId}
        LIMIT 1
    """)
    Optional<ShippingProviderConfig> findByTenantIdAndProviderId(@Param("tenantId") Long tenantId, @Param("providerId") Long providerId);

    @Select("""
        SELECT * FROM ecommerce_shipment.shipping_provider_config
        WHERE tenant_id = #{tenantId}
        ORDER BY id ASC
    """)
    java.util.List<ShippingProviderConfig> findByTenantId(@Param("tenantId") Long tenantId);
}
