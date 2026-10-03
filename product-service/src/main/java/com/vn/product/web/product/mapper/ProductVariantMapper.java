package com.vn.product.web.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.product.web.product.domain.entity.ProductVariant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.UUID;

@Mapper
public interface ProductVariantMapper extends BaseMapper<ProductVariant> {

    @Update("UPDATE ecommerce_product.product_variants SET is_default = false WHERE product_id = #{productId}")
    void resetDefaultVariant(@Param("productId") UUID productId);
}
