package com.vn.order.core.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.StringUtils;

public class PageableUtils {

    private static final long DEFAULT_PAGE = 1;
    private static final long DEFAULT_SIZE = 10;
    private static final long MAX_SIZE = 100;

    public static <T> Page<T> createPage(Integer page, Integer size) {
        long effectivePage = (page != null && page > 0) ? page : DEFAULT_PAGE;
        long effectiveSize = calculateEffectiveSize(size);
        return new Page<>(effectivePage, effectiveSize);
    }

    public static org.springframework.data.domain.Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.unsorted();
        if (StringUtils.hasText(sortBy)) {
            org.springframework.data.domain.Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                    ? org.springframework.data.domain.Sort.Direction.ASC
                    : org.springframework.data.domain.Sort.Direction.DESC;
            sort = org.springframework.data.domain.Sort.by(direction, sortBy);
        }
        return org.springframework.data.domain.PageRequest.of(Math.max(0, page), size > 0 ? size : (int) DEFAULT_SIZE, sort);
    }

    private static long calculateEffectiveSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
