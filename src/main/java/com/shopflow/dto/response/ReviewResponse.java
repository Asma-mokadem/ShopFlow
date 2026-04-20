package com.shopflow.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long id;
    private Integer rating;
    private String comment;
    private String customerName;
    private String customerEmail;
    private Long productId;
    private String productName;
    private LocalDateTime createdAt;
}