package com.vn.product.web.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vn.product.core.exception.AppException;
import com.vn.product.core.exception.ErrorCode;
import com.vn.product.core.model.PageResponse;
import com.vn.product.web.category.domain.entity.Category;
import com.vn.product.web.category.domain.request.CategoryCreateRequest;
import com.vn.product.web.category.domain.request.CategoryFilter;
import com.vn.product.web.category.domain.request.CategoryUpdateRequest;
import com.vn.product.web.category.domain.response.CategoryResponse;
import com.vn.product.web.category.domain.response.CategorySelectResponse;
import com.vn.product.web.category.domain.response.CategoryTreeResponse;
import com.vn.product.web.category.mapper.CategoryMapper;
import com.vn.product.web.category.service.ICategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "Category-Service")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Override
    public PageResponse<CategoryResponse> getAllCategories(CategoryFilter filter, Pageable pageable) {
        Page<Category> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();

        if (filter != null) {
            if (StringUtils.hasText(filter.getName())) {
                wrapper.like(Category::getName, filter.getName());
            }
            if (StringUtils.hasText(filter.getSlug())) {
                wrapper.like(Category::getSlug, filter.getSlug());
            }
            if (filter.getParentId() != null) {
                wrapper.eq(Category::getParentId, filter.getParentId());
            }
            if (filter.getIsActive() != null) {
                wrapper.eq(Category::getIsActive, filter.getIsActive());
            }
        }
        wrapper.orderByAsc(Category::getSortOrder).orderByAsc(Category::getCreatedAt);

        IPage<Category> result = baseMapper.selectPage(page, wrapper);
        List<CategoryResponse> responses = result.getRecords().stream()
                .map(this::mapToCategoryResponse)
                .toList();

        return PageResponse.<CategoryResponse>builder()
                .content(responses)
                .pageNumber((int) result.getCurrent())
                .pageSize((int) result.getSize())
                .totalElements(result.getTotal())
                .totalPages((int) result.getPages())
                .last(result.getCurrent() >= result.getPages())
                .first(result.getCurrent() <= 1)
                .empty(responses.isEmpty())
                .build();
    }

    @Override
    public CategoryResponse getCategoryById(UUID id) {
        Category category = baseMapper.selectById(id);
        if (category == null) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return mapToCategoryResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorySelectResponse> getAvailableParents(UUID excludeCategoryId) {
        List<Category> allCategories = baseMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getIsActive, true)
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getName));

        Set<UUID> excludedIds = new HashSet<>();
        if (excludeCategoryId != null) {
            excludedIds.add(excludeCategoryId);
            try {
                excludedIds.addAll(baseMapper.findAllDescendantIds(excludeCategoryId));
            } catch (Exception ignored) {}
        }

        return allCategories.stream()
                .filter(cat -> !excludedIds.contains(cat.getId()))
                .map(cat -> CategorySelectResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .parentId(cat.getParentId())
                        .build())
                .toList();
    }

    @Override
    @Transactional(rollbackFor = AppException.class)
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        Long existingCount = baseMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getSlug, request.getSlug()));
        if (existingCount != null && existingCount > 0) {
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        if (request.getParentId() != null) {
            Category parent = baseMapper.selectById(request.getParentId());
            if (parent == null) {
                throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
            }
        }

        Category category = Category.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .parentId(request.getParentId())
                .imageUrl(request.getImageUrl())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        baseMapper.insert(category);
        log.debug("Created Category: {}", category.getName());
        return mapToCategoryResponse(category);
    }

    @Override
    @Transactional(rollbackFor = AppException.class)
    public void updateCategory(UUID id, CategoryUpdateRequest request) {
        Category category = baseMapper.selectById(id);
        if (category == null) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        Long slugCount = baseMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getSlug, request.getSlug())
                .ne(Category::getId, id));
        if (slugCount != null && slugCount > 0) {
            throw new AppException(ErrorCode.SLUG_ALREADY_EXISTS);
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new AppException(ErrorCode.CATEGORY_CIRCULAR_REFERENCE);
            }
            Category parent = baseMapper.selectById(request.getParentId());
            if (parent == null) {
                throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
            }
        }

        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        category.setParentId(request.getParentId());
        category.setImageUrl(request.getImageUrl());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());
        if (request.getIsActive() != null) category.setActive(request.getIsActive());
        category.setUpdatedAt(LocalDateTime.now());

        baseMapper.updateById(category);
        log.debug("Category updated: {}", category.getName());
    }

    @Override
    @Transactional(rollbackFor = AppException.class)
    public void deleteCategory(UUID id) {
        Category category = baseMapper.selectById(id);
        if (category == null) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        Long childCount = baseMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new AppException(ErrorCode.CATEGORY_HAS_CHILDREN);
        }

        baseMapper.deleteById(id);
        log.debug("Category deleted: {}", category.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        List<Category> rootCategories = baseMapper.selectList(new LambdaQueryWrapper<Category>()
                .isNull(Category::getParentId)
                .eq(Category::getIsActive, true)
                .orderByAsc(Category::getSortOrder));

        return rootCategories.stream()
                .map(this::mapToCategoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getChildCategories(UUID parentId) {
        return baseMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, parentId)
                .eq(Category::getIsActive, true)
                .orderByAsc(Category::getSortOrder))
                .stream()
                .map(this::mapToCategoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> allCategories = baseMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getIsActive, true)
                .orderByAsc(Category::getSortOrder));

        Map<UUID, List<Category>> childrenByParentId = allCategories.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Category::getParentId));

        for (Category cat : allCategories) {
            cat.setChildren(childrenByParentId.getOrDefault(cat.getId(), Collections.emptyList()));
        }

        return allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .map(this::buildTreeNode)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTreeByParentId(UUID parentId) {
        List<Category> children = baseMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, parentId)
                .eq(Category::getIsActive, true)
                .orderByAsc(Category::getSortOrder));

        return children.stream()
                .map(this::buildTreeNode)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getAllDescendantCategoryIds(UUID categoryId) {
        Category root = baseMapper.selectById(categoryId);
        if (root == null) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return baseMapper.findAllDescendantIds(categoryId);
    }

    private CategoryTreeResponse buildTreeNode(Category category) {
        CategoryTreeResponse node = CategoryTreeResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParentId())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .sortOrder(category.getSortOrder())
                .isActive(category.getIsActive())
                .build();

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            List<CategoryTreeResponse> children = category.getChildren().stream()
                    .filter(Category::isActive)
                    .sorted((a, b) -> {
                        int cmp = Integer.compare(
                                a.getSortOrder() != null ? a.getSortOrder() : 0,
                                b.getSortOrder() != null ? b.getSortOrder() : 0
                        );
                        return cmp != 0 ? cmp : a.getName().compareTo(b.getName());
                    })
                    .map(this::buildTreeNode)
                    .toList();
            node.setChildren(children);
        }

        return node;
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        String parentName = null;
        if (category.getParentId() != null) {
            Category parent = baseMapper.selectById(category.getParentId());
            if (parent != null) {
                parentName = parent.getName();
            }
        }
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParentId())
                .parentName(parentName)
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .sortOrder(category.getSortOrder())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
