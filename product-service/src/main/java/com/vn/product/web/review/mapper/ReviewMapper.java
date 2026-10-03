package com.vn.product.web.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.product.web.review.domain.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.UUID;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Select("SELECT COALESCE(AVG(rating), 0) FROM ecommerce_product.reviews WHERE product_id = #{productId} AND status = 'APPROVED'")
    BigDecimal calculateAverageRating(@Param("productId") UUID productId);

    @Select("SELECT COUNT(*) FROM ecommerce_product.reviews WHERE product_id = #{productId} AND status = 'APPROVED'")
    Integer countApprovedReviews(@Param("productId") UUID productId);
}
