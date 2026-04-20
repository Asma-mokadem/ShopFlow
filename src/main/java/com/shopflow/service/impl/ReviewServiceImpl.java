package com.shopflow.service.impl;

import com.shopflow.dto.request.ReviewRequest;
import com.shopflow.dto.response.ReviewResponse;
import com.shopflow.entity.*;
import com.shopflow.repository.*;
import com.shopflow.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    public ReviewResponse addReview(String userEmail,
                                    Long productId,
                                    ReviewRequest request) {

        // Vérifier que le produit existe
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Vérifier que l'utilisateur n'a pas déjà laissé un avis
        if (reviewRepository.existsByUserEmailAndProductId(userEmail, productId)) {
            throw new RuntimeException("You have already reviewed this product");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Créer l'avis
        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);

        // Mettre à jour la moyenne du produit
        updateProductRating(product);

        return toResponse(review);
    }

    @Override
    public ReviewResponse updateReview(String userEmail,
                                       Long reviewId,
                                       ReviewRequest request) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Vérifier que c'est l'auteur
        if (!review.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("You are not authorized to update this review");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        reviewRepository.save(review);

        // Recalculer la moyenne
        updateProductRating(review.getProduct());

        return toResponse(review);
    }

    @Override
    public void deleteReview(String userEmail, Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("You are not authorized to delete this review");
        }

        Product product = review.getProduct();
        reviewRepository.delete(review);

        // Recalculer la moyenne
        updateProductRating(product);
    }

    @Override
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<ReviewResponse> getMyReviews(String userEmail, Pageable pageable) {
        // Récupérer tous les avis et filtrer par email
        return reviewRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // ===== HELPERS =====

    private void updateProductRating(Product product) {
        Double avg = reviewRepository
                .calculateAverageRating(product.getId());
        product.setAverageRating(avg != null ? avg : 0.0);
        productRepository.save(product);
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .customerName(review.getUser().getFirstName()
                        + " " + review.getUser().getLastName())
                .customerEmail(review.getUser().getEmail())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .createdAt(review.getCreatedAt())
                .build();
    }
}