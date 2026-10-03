package com.vn.product.web.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vn.product.core.enums.ProductStatus;
import com.vn.product.core.exception.AppException;
import com.vn.product.core.exception.ErrorCode;
import com.vn.product.core.model.PageResponse;
import com.vn.product.core.security.UserContext;
import com.vn.product.web.category.domain.entity.Category;
import com.vn.product.web.category.domain.response.CategoryResponse;
import com.vn.product.web.category.mapper.CategoryMapper;
import com.vn.product.web.product.domain.entity.Product;
import com.vn.product.web.product.domain.entity.ProductImage;
import com.vn.product.web.product.domain.entity.ProductVariant;
import com.vn.product.web.product.domain.request.*;
import com.vn.product.web.product.domain.response.*;
import com.vn.product.web.product.mapper.ProductImageMapper;
import com.vn.product.web.product.mapper.ProductMapper;
import com.vn.product.web.product.mapper.ProductVariantMapper;
import com.vn.product.web.product.service.IProductImageService;
import com.vn.product.web.product.service.IProductService;
import com.vn.product.web.product.service.IProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "Product-Service")
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    private final ProductVariantMapper productVariantMapper;
    private final ProductImageMapper productImageMapper;
    private final CategoryMapper categoryMapper;
    private final IProductVariantService productVariantService;
    private final IProductImageService productImageService;

    @Override
    public PageResponse<ProductResponse> getProducts(ProductFilter filter, Pageable pageable) {
        Page<Product> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (filter != null) {
            if (StringUtils.hasText(filter.getKeyword())) {
                wrapper.and(w -> w.like(Product::getName, filter.getKeyword())
                        .or().like(Product::getDescription, filter.getKeyword())
                        .or().like(Product::getBrand, filter.getKeyword()));
            }
            if (filter.getCategoryId() != null) {
                wrapper.eq(Product::getCategoryId, filter.getCategoryId());
            }
            if (filter.getStoreId() != null) {
                wrapper.eq(Product::getStoreId, filter.getStoreId());
            }
            if (StringUtils.hasText(filter.getBrand())) {
                wrapper.like(Product::getBrand, filter.getBrand());
            }
            if (StringUtils.hasText(filter.getStatus())) {
                wrapper.eq(Product::getStatus, filter.getStatus().toUpperCase());
            }
            if (filter.getMinPrice() != null) {
                wrapper.inSql(Product::getId, "SELECT product_id FROM ecommerce_product.product_variants WHERE price >= " + filter.getMinPrice());
            }
            if (filter.getMaxPrice() != null) {
                wrapper.inSql(Product::getId, "SELECT product_id FROM ecommerce_product.product_variants WHERE price <= " + filter.getMaxPrice());
            }
            if (Boolean.TRUE.equals(filter.getInStock())) {
                wrapper.inSql(Product::getId, "SELECT product_id FROM ecommerce_product.product_variants WHERE stock_quantity > 0");
            }
        }
        wrapper.orderByDesc(Product::getCreatedAt);

        IPage<Product> result = baseMapper.selectPage(page, wrapper);
        List<Product> products = result.getRecords();

        if (products.isEmpty()) {
            return PageResponse.<ProductResponse>builder()
                    .content(Collections.emptyList())
                    .pageNumber((int) result.getCurrent())
                    .pageSize((int) result.getSize())
                    .totalElements(result.getTotal())
                    .totalPages((int) result.getPages())
                    .last(true)
                    .first(true)
                    .empty(true)
                    .build();
        }

        List<UUID> productIds = products.stream().map(Product::getId).toList();

        // Batch fetch variants
        List<ProductVariant> variants = productVariantMapper.selectList(
                new LambdaQueryWrapper<ProductVariant>()
                        .in(ProductVariant::getProductId, productIds)
                        .orderByDesc(ProductVariant::getIsDefault));
        Map<UUID, List<ProductVariant>> variantsByProductId = variants.stream()
                .collect(Collectors.groupingBy(ProductVariant::getProductId));

        // Batch fetch primary images
        List<ProductImage> images = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .in(ProductImage::getProductId, productIds)
                        .eq(ProductImage::getIsPrimary, true));
        Map<UUID, ProductImage> mainImageByProductId = images.stream()
                .collect(Collectors.toMap(ProductImage::getProductId, img -> img, (existing, replacement) -> existing));

        // Batch fetch categories
        List<UUID> categoryIds = products.stream().map(Product::getCategoryId).filter(Objects::nonNull).toList();
        Map<UUID, Category> categoryById = categoryIds.isEmpty() ? Collections.emptyMap() :
                categoryMapper.selectBatchIds(categoryIds).stream().collect(Collectors.toMap(Category::getId, c -> c));

        List<ProductResponse> content = products.stream().map(product -> {
            Category cat = product.getCategoryId() != null ? categoryById.get(product.getCategoryId()) : null;
            List<ProductVariant> prodVariants = variantsByProductId.getOrDefault(product.getId(), Collections.emptyList());
            ProductImage mainImg = mainImageByProductId.get(product.getId());

            BigDecimal minPrice = null;
            BigDecimal maxPrice = null;
            int totalStock = 0;

            for (ProductVariant v : prodVariants) {
                if (v.getPrice() != null) {
                    if (minPrice == null || v.getPrice().compareTo(minPrice) < 0) minPrice = v.getPrice();
                    if (maxPrice == null || v.getPrice().compareTo(maxPrice) > 0) maxPrice = v.getPrice();
                }
                if (v.getStockQuantity() != null) {
                    totalStock += v.getStockQuantity();
                }
            }

            return ProductResponse.builder()
                    .id(product.getId())
                    .storeId(product.getStoreId())
                    .categoryId(product.getCategoryId())
                    .categoryName(cat != null ? cat.getName() : null)
                    .name(product.getName())
                    .slug(product.getSlug())
                    .description(product.getDescription())
                    .brand(product.getBrand())
                    .status(product.getStatus())
                    .mainImageUrl(mainImg != null ? mainImg.getUrl() : null)
                    .minPrice(minPrice)
                    .maxPrice(maxPrice)
                    .totalStock(totalStock)
                    .ratingAvg(product.getRatingAvg())
                    .ratingCount(product.getRatingCount())
                    .createdAt(product.getCreatedAt())
                    .updatedAt(product.getUpdatedAt())
                    .build();
        }).toList();

        return PageResponse.<ProductResponse>builder()
                .content(content)
                .pageNumber((int) result.getCurrent())
                .pageSize((int) result.getSize())
                .totalElements(result.getTotal())
                .totalPages((int) result.getPages())
                .last(result.getCurrent() >= result.getPages())
                .first(result.getCurrent() <= 1)
                .empty(content.isEmpty())
                .build();
    }

    @Override
    public ProductDetailResponse getProductById(UUID id) {
        Product product = baseMapper.selectById(id);
        if (product == null) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return mapToProductDetailResponse(product);
    }

    @Override
    public ProductDetailResponse getProductBySlug(String slug) {
        Product product = baseMapper.selectOne(new LambdaQueryWrapper<Product>().eq(Product::getSlug, slug));
        if (product == null) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return mapToProductDetailResponse(product);
    }

    @Override
    @Transactional(rollbackFor = AppException.class)
    public ProductResponse createProduct(ProductCreateRequest request) {
        UUID storeId = request.getStoreId();
        if (storeId == null) {
            storeId = UserContext.getStoreId();
        }
        if (storeId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String slug = request.getSlug();
        if (!StringUtils.hasText(slug)) {
            slug = (request.getName() != null ? request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-") : "prod") + "-" + UUID.randomUUID().toString().substring(0, 8);
        }

        Long existingCount = baseMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getSlug, slug));
        if (existingCount != null && existingCount > 0) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 6);
        }

        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .id(productId)
                .storeId(storeId)
                .categoryId(request.getCategoryId())
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .brand(request.getBrand())
                .status(StringUtils.hasText(request.getStatus()) ? request.getStatus().toUpperCase() : ProductStatus.DRAFT.name())
                .attributes(request.getAttributes())
                .ratingAvg(BigDecimal.ZERO)
                .ratingCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        baseMapper.insert(product);

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (ProductVariantCreateRequest variantRequest : request.getVariants()) {
                productVariantService.createVariant(product.getId(), variantRequest);
            }
        }

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (ProductImageCreateRequest imageRequest : request.getImages()) {
                productImageService.addImage(product.getId(), imageRequest);
            }
        }

        log.debug("Created product with id {} for storeId {}", product.getId(), storeId);

        return ProductResponse.builder()
                .id(product.getId())
                .storeId(product.getStoreId())
                .categoryId(product.getCategoryId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .brand(product.getBrand())
                .status(product.getStatus())
                .ratingAvg(product.getRatingAvg())
                .ratingCount(product.getRatingCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = AppException.class)
    public void updateProduct(UUID id, ProductUpdateRequest request) {
        Product product = baseMapper.selectById(id);
        if (product == null) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        if (request.getName() != null) product.setName(request.getName());
        if (request.getSlug() != null) product.setSlug(request.getSlug());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getBrand() != null) product.setBrand(request.getBrand());
        if (request.getCategoryId() != null) product.setCategoryId(request.getCategoryId());
        if (request.getStatus() != null) product.setStatus(request.getStatus().toUpperCase());
        if (request.getAttributes() != null) product.setAttributes(request.getAttributes());
        product.setUpdatedAt(LocalDateTime.now());

        baseMapper.updateById(product);

        if (request.getVariants() != null) {
            for (ProductVariantUpdateRequest variantRequest : request.getVariants()) {
                if (variantRequest.getId() != null) {
                    productVariantService.updateVariant(variantRequest.getId(), variantRequest);
                }
            }
        }

        log.debug("Updated product with id {}", product.getId());
    }

    @Override
    @Transactional(rollbackFor = AppException.class)
    public void deleteProduct(UUID id) {
        Product product = baseMapper.selectById(id);
        if (product == null) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        productVariantService.deleteVariantsByProductId(id);
        productImageService.deleteAllImagesByProductId(id);
        baseMapper.deleteById(id);

        log.debug("Deleted product with id {}", id);
    }

    @Override
    @Transactional(rollbackFor = AppException.class)
    public void updateProductStatus(UUID id, String status) {
        Product product = baseMapper.selectById(id);
        if (product == null) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        product.setStatus(status.toUpperCase());
        product.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(product);
    }

    private ProductDetailResponse mapToProductDetailResponse(Product product) {
        List<ProductVariantResponse> variants = productVariantService.getVariantsByProductId(product.getId());
        List<ProductImageResponse> images = productImageService.getImagesByProductId(product.getId());

        CategoryResponse categoryResponse = null;
        if (product.getCategoryId() != null) {
            Category cat = categoryMapper.selectById(product.getCategoryId());
            if (cat != null) {
                categoryResponse = CategoryResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .slug(cat.getSlug())
                        .description(cat.getDescription())
                        .imageUrl(cat.getImageUrl())
                        .build();
            }
        }

        return ProductDetailResponse.builder()
                .id(product.getId())
                .storeId(product.getStoreId())
                .categoryId(product.getCategoryId())
                .category(categoryResponse)
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .brand(product.getBrand())
                .status(product.getStatus())
                .attributes(product.getAttributes())
                .ratingAvg(product.getRatingAvg())
                .ratingCount(product.getRatingCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .variants(variants)
                .images(images)
                .build();
    }
}
