package com.vn.product.web.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vn.product.web.category.domain.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.UUID;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("""
        WITH RECURSIVE category_tree AS (
            SELECT id FROM ecommerce_product.categories WHERE id = #{categoryId}
            UNION ALL
            SELECT c.id FROM ecommerce_product.categories c
            INNER JOIN category_tree ct ON c.parent_id = ct.id
        )
        SELECT id FROM category_tree
    """)
    List<UUID> findAllDescendantIds(@Param("categoryId") UUID categoryId);
}
