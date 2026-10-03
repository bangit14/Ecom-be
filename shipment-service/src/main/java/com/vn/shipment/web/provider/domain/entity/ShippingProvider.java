package com.vn.shipment.web.provider.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "shipping_provider", schema = "ecommerce_shipment")
public class ShippingProvider {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;
    private String name;
    private String logoUrl;

    @Builder.Default
    private Boolean supportsCod = true;

    @Builder.Default
    private Boolean supportsInsurance = false;

    @Builder.Default
    private Boolean supportsWebhook = true;

    private String trackingUrlTemplate;

    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
