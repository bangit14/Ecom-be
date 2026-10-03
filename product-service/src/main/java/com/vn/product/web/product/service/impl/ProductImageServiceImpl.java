package com.vn.product.web.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vn.product.core.exception.AppException;
import com.vn.product.core.exception.ErrorCode;
import com.vn.product.core.storage.MinioStorageService;
import com.vn.product.web.product.domain.entity.Product;
import com.vn.product.web.product.domain.entity.ProductImage;
import com.vn.product.web.product.domain.entity.ProductVariant;
import com.vn.product.web.product.domain.request.ProductImageCreateRequest;
import com.vn.product.web.product.domain.response.ProductImageResponse;
import com.vn.product.web.product.mapper.ProductImageMapper;
import com.vn.product.web.product.mapper.ProductMapper;
import com.vn.product.web.product.mapper.ProductVariantMapper;
import com.vn.product.web.product.service.IProductImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "Product-Image-Service")
public class ProductImageServiceImpl extends ServiceImpl<ProductImageMapper, ProductImage> implements IProductImageService {

    private final MinioStorageService minioStorageService;
    private final ProductMapper productMapper;
    private final ProductVariantMapper productVariantMapper;

    @Override
    public List<ProductImageResponse> getImagesByProductId(UUID productId) {
        List<ProductImage> images = baseMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
                        .orderByAsc(ProductImage::getSortOrder));

        return images.stream().map(
                img -> ProductImageResponse.builder()
                        .id(img.getId())
                        .productId(img.getProductId())
                        .variantId(img.getVariantId())
                        .storageProvider(img.getStorageProvider())
                        .bucket(img.getBucket())
                        .objectKey(img.getObjectKey())
                        .url(img.getUrl())
                        .altText(img.getAltText())
                        .sortOrder(img.getSortOrder())
                        .isPrimary(img.isPrimary())
                        .createdAt(img.getCreatedAt())
                        .build()
        ).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductImageResponse addImage(UUID productId, ProductImageCreateRequest request) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        if (request.getVariantId() != null) {
            ProductVariant variant = productVariantMapper.selectById(request.getVariantId());
            if (variant == null) {
                throw new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND);
            }
        }

        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            baseMapper.updateIsPrimaryToFalse(productId);
        }

        ProductImage productImage = ProductImage.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .variantId(request.getVariantId())
                .storageProvider(request.getStorageProvider() != null ? request.getStorageProvider() : "minio")
                .bucket(request.getBucket() != null ? request.getBucket() : "ecommerce-products")
                .objectKey(request.getObjectKey() != null ? request.getObjectKey() : "")
                .url(request.getUrl())
                .altText(request.getAltText())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : getNextSortOrder(productId))
                .isPrimary(Boolean.TRUE.equals(request.getIsPrimary()))
                .createdAt(LocalDateTime.now())
                .build();

        baseMapper.insert(productImage);
        log.debug("Saved image metadata successfully for productId={}", productId);

        return ProductImageResponse.builder()
                .id(productImage.getId())
                .productId(productImage.getProductId())
                .variantId(productImage.getVariantId())
                .storageProvider(productImage.getStorageProvider())
                .bucket(productImage.getBucket())
                .objectKey(productImage.getObjectKey())
                .url(productImage.getUrl())
                .altText(productImage.getAltText())
                .sortOrder(productImage.getSortOrder())
                .isPrimary(productImage.isPrimary())
                .createdAt(productImage.getCreatedAt())
                .build();
    }

    @Override
    public void deleteImage(UUID imageId) {
        ProductImage image = baseMapper.selectById(imageId);
        if (image == null) {
            throw new AppException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
        }

        if (image.getUrl() != null) {
            try {
                minioStorageService.deleteFile(image.getUrl());
            } catch (Exception e) {
                log.warn("Failed to delete file from MinIO, continuing to delete from DB: {}", image.getUrl());
            }
        }

        baseMapper.deleteById(imageId);
        log.info("Deleted image with id: {}", imageId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAllImagesByProductId(UUID productId) {
        List<ProductImage> images = baseMapper.selectList(
                new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, productId));

        for (ProductImage image : images) {
            if (image.getUrl() != null) {
                try {
                    minioStorageService.deleteFile(image.getUrl());
                } catch (Exception e) {
                    log.warn("Failed to delete MinIO file: {}", image.getUrl());
                }
            }
        }

        baseMapper.delete(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, productId));
        log.info("Deleted all images for productId: {}", productId);
    }

    private Integer getNextSortOrder(UUID productId) {
        Integer maxOrder = baseMapper.getMaxSortOrderByProductId(productId);
        return maxOrder == null ? 0 : maxOrder + 10;
    }
}
