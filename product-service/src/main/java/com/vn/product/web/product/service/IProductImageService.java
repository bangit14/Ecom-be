package com.vn.product.web.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vn.product.web.product.domain.entity.ProductImage;
import com.vn.product.web.product.domain.request.ProductImageCreateRequest;
import com.vn.product.web.product.domain.response.ProductImageResponse;

import java.util.List;
import java.util.UUID;

public interface IProductImageService extends IService<ProductImage> {

    List<ProductImageResponse> getImagesByProductId(UUID productId);

    ProductImageResponse addImage(UUID productId, ProductImageCreateRequest request);

    void deleteAllImagesByProductId(UUID productId);

    void deleteImage(UUID imageId);
}
