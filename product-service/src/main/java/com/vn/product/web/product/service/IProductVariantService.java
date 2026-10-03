package com.vn.product.web.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vn.product.web.product.domain.entity.ProductVariant;
import com.vn.product.web.product.domain.request.ProductVariantCreateRequest;
import com.vn.product.web.product.domain.request.ProductVariantUpdateRequest;
import com.vn.product.web.product.domain.response.ProductVariantResponse;

import java.util.List;
import java.util.UUID;

public interface IProductVariantService extends IService<ProductVariant> {

    List<ProductVariantResponse> getVariantsByProductId(UUID productId);

    ProductVariantResponse createVariant(UUID productId, ProductVariantCreateRequest request);

    void updateVariant(UUID variantId, ProductVariantUpdateRequest request);

    void deleteVariant(UUID variantId);

    void deleteVariantsByProductId(UUID productId);
}
