package com.vn.order.web.order.domain.response;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    private boolean empty;

    public static <T> PageResponse<T> from(IPage<T> page) {
        return PageResponse.<T>builder()
                .content(page.getRecords())
                .pageNumber((int) page.getCurrent())
                .pageSize((int) page.getSize())
                .totalElements(page.getTotal())
                .totalPages((int) page.getPages())
                .last(page.getCurrent() >= page.getPages())
                .first(page.getCurrent() <= 1)
                .empty(page.getRecords() == null || page.getRecords().isEmpty())
                .build();
    }
}
