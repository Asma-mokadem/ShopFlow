package com.shopflow.service;

import com.shopflow.dto.request.ReviewRequest;
import com.shopflow.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponse addReview(String userEmail, Long productId, ReviewRequest request);
    ReviewResponse updateReview(String userEmail, Long reviewId, ReviewRequest request);
    void deleteReview(String userEmail, Long reviewId);
    Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable);
    Page<ReviewResponse> getMyReviews(String userEmail, Pageable pageable);
}