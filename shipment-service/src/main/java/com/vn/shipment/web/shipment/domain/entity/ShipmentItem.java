package com.vn.shipment.web.shipment.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "shipment_item", schema = "ecommerce_shipment")
public class ShipmentItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shipmentId;
    private Long orderItemId;

    private String productName;
    private String sku;

    @Builder.Default
    private Integer quantity = 1;

    private Integer unitWeightGram;
    private BigDecimal unitPrice;
    private String category;
}
