package com.vn.product.web.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.product.web.product.domain.entity.ProductImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.UUID;

@Mapper
public interface ProductImageMapper extends BaseMapper<ProductImage> {

    @Update("UPDATE ecommerce_product.product_images SET is_primary = false WHERE product_id = #{productId} AND variant_id IS NULL")
    void updateIsPrimaryToFalse(@Param("productId") UUID productId);

    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM ecommerce_product.product_images WHERE product_id = #{productId}")
    Integer getMaxSortOrderByProductId(@Param("productId") UUID productId);
}
