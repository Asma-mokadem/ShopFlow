package com.shopflow.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String status;
    private BigDecimal totalAmount;
    private String couponCode;
    private List<OrderItemResponse> items;
    private String customerEmail;
    private LocalDateTime createdAt;
}