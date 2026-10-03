package com.vn.shipment.web.address.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "shipping_ward", schema = "ecommerce_shipment")
public class ShippingWard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long providerId;
    private Long districtRefId;
    private String externalCode;
    private String wardName;
    private String standardCode;
}
