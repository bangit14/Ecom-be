package com.vn.order.web.cart.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    private Long userId;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    private LocalDateTime lastUpdated;

    public BigDecimal getTotalAmount() {
        if (items == null) return BigDecimal.ZERO;
        return items.stream()
                .map(item -> item.getPrice() != null && item.getQuantity() != null
                        ? item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalQuantity() {
        if (items == null) return 0;
        return items.stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();
    }

    public void addItem(CartItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }
}
