package com.vn.product.web.product;

import com.vn.product.core.model.PageResponse;
import com.vn.product.core.util.PageableUtils;
import com.vn.product.web.product.domain.request.*;
import com.vn.product.web.product.domain.response.ProductDetailResponse;
import com.vn.product.web.product.domain.response.ProductImageResponse;
import com.vn.product.web.product.domain.response.ProductResponse;
import com.vn.product.web.product.domain.response.ProductVariantResponse;
import com.vn.product.web.product.service.IProductImageService;
import com.vn.product.web.product.service.IProductService;
import com.vn.product.web.product.service.IProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j(topic = "Product-Controller")
public class ProductController {

    private final IProductService productService;
    private final IProductVariantService productVariantService;
    private final IProductImageService productImageService;

    @GetMapping("/list")
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(
            @ModelAttribute ProductFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {

        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, sortDir);
        return ResponseEntity.ok(productService.getProducts(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponse> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductDetailResponse> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }

    @PostMapping("/create")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updateProduct(@PathVariable UUID id, @RequestBody ProductUpdateRequest request) {
        productService.updateProduct(id, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateProductStatus(@PathVariable UUID id, @RequestParam String status) {
        productService.updateProductStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<ProductImageResponse>> getProductImages(@PathVariable UUID id) {
        return ResponseEntity.ok(productImageService.getImagesByProductId(id));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ProductImageResponse> addProductImage(@PathVariable UUID id, @RequestBody ProductImageCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productImageService.addImage(id, request));
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteProductImage(@PathVariable UUID imageId) {
        productImageService.deleteImage(imageId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}/variants")
    public ResponseEntity<List<ProductVariantResponse>> getProductVariants(@PathVariable UUID id) {
        return ResponseEntity.ok(productVariantService.getVariantsByProductId(id));
    }

    @PostMapping("/{id}/variants")
    public ResponseEntity<ProductVariantResponse> addProductVariant(@PathVariable UUID id, @RequestBody ProductVariantCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productVariantService.createVariant(id, request));
    }

    @PutMapping("/variants/{variantId}")
    public ResponseEntity<Void> updateProductVariant(@PathVariable UUID variantId, @RequestBody ProductVariantUpdateRequest request) {
        productVariantService.updateVariant(variantId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<Void> deleteProductVariant(@PathVariable UUID variantId) {
        productVariantService.deleteVariant(variantId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
