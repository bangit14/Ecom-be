package com.vn.product.web.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vn.product.core.model.PageResponse;
import com.vn.product.web.product.domain.entity.Product;
import com.vn.product.web.product.domain.request.ProductCreateRequest;
import com.vn.product.web.product.domain.request.ProductFilter;
import com.vn.product.web.product.domain.request.ProductUpdateRequest;
import com.vn.product.web.product.domain.response.ProductDetailResponse;
import com.vn.product.web.product.domain.response.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IProductService extends IService<Product> {

    PageResponse<ProductResponse> getProducts(ProductFilter filter, Pageable pageable);

    ProductDetailResponse getProductById(UUID id);

    ProductDetailResponse getProductBySlug(String slug);

    ProductResponse createProduct(ProductCreateRequest request);

    void updateProduct(UUID id, ProductUpdateRequest request);

    void deleteProduct(UUID id);

    void updateProductStatus(UUID id, String status);
}
