package com.vn.order.web.order.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vn.order.core.enums.OrderStatus;
import com.vn.order.core.enums.PaymentStatus;
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
@TableName(value = "orders", schema = "ecommerce_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderCode;
    private Long userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    private String note;

    // Snapshot Shipping Address
    private Long shippingAddressId;
    private String shippingFullName;
    private String shippingPhone;
    private String shippingAddressLine;
    private String shippingWard;
    private String shippingDistrict;
    private String shippingProvince;
    private String shippingCountry;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @TableField(exist = false)
    @Builder.Default
    private List<OrderHistory> histories = new ArrayList<>();

    public void addItem(OrderItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    public void calculateTotals() {
        if (items != null) {
            this.totalAmount = items.stream()
                    .map(OrderItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            this.totalAmount = BigDecimal.ZERO;
        }

        BigDecimal discount = this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO;
        BigDecimal shipping = this.shippingFee != null ? this.shippingFee : BigDecimal.ZERO;

        this.finalAmount = this.totalAmount.subtract(discount).add(shipping);
    }

    public boolean isCancellable() {
        return this.status == OrderStatus.PENDING || this.status == OrderStatus.CONFIRMED;
    }
}
