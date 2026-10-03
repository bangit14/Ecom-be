package com.vn.shipment.web.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.shipment.web.provider.domain.entity.ShippingProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface ShippingProviderMapper extends BaseMapper<ShippingProvider> {

    @Select("SELECT * FROM ecommerce_shipment.shipping_provider WHERE code = #{code} AND is_active = true LIMIT 1")
    Optional<ShippingProvider> findByCode(@Param("code") String code);
}
