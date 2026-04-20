package com.shopflow.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private Long id;
    private Long productVariantId;
    private String productName;
    private String size;
    private String color;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}