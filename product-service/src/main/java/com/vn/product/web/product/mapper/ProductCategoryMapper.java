package com.vn.product.web.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.product.web.product.domain.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
}
