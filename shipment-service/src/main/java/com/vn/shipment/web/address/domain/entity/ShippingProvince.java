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
@TableName(value = "shipping_province", schema = "ecommerce_shipment")
public class ShippingProvince {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long providerId;
    private String externalCode;
    private String provinceName;
    private String standardCode;
}
