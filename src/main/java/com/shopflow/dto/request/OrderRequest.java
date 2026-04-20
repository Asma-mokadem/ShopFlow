package com.shopflow.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    // Adresse de livraison
    @NotNull(message = "Address ID is required")
    private Long addressId;

    // Code promo optionnel
    private String couponCode;
}