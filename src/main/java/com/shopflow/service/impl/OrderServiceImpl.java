package com.shopflow.service.impl;

import com.shopflow.dto.request.OrderRequest;
import com.shopflow.dto.response.OrderItemResponse;
import com.shopflow.dto.response.OrderResponse;
import com.shopflow.entity.*;
import com.shopflow.repository.*;
import com.shopflow.service.CartService;
import com.shopflow.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CouponRepository couponRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public OrderResponse createOrder(String userEmail, OrderRequest request) {

        // 1. Récupérer le panier
        Cart cart = cartRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 2. Vérifier l'adresse
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Address does not belong to you");
        }

        // 3. Calculer le total
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getProductVariant()
                        .getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Appliquer le coupon si présent
        String couponCode = null;
        if (request.getCouponCode() != null && !request.getCouponCode().isEmpty()) {
            Coupon coupon = couponRepository
                    .findByCode(request.getCouponCode())
                    .orElseThrow(() -> new RuntimeException("Invalid coupon code"));

            if (!coupon.isActive()) {
                throw new RuntimeException("Coupon is not active");
            }
            if (coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Coupon has expired");
            }
            if (coupon.getUsedCount() >= coupon.getMaxUses()) {
                throw new RuntimeException("Coupon has reached max uses");
            }

            // Appliquer la réduction
            BigDecimal discount = total.multiply(
                    coupon.getDiscountPercent().divide(BigDecimal.valueOf(100)));
            total = total.subtract(discount);

            // Mettre à jour le coupon
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
            couponCode = coupon.getCode();
        }

        // 5. Créer la commande
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = Order.builder()
                .user(user)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(total)
                .couponCode(couponCode)
                .build();

        Order savedOrder = orderRepository.save(order);

        // 6. Créer les OrderItems + déduire le stock
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> {
                    ProductVariant variant = cartItem.getProductVariant();

                    // Vérifier et déduire le stock
                    if (variant.getStock() < cartItem.getQuantity()) {
                        throw new RuntimeException(
                                "Insufficient stock for: " +
                                        variant.getProduct().getName());
                    }
                    variant.setStock(variant.getStock() - cartItem.getQuantity());
                    productVariantRepository.save(variant);

                    return OrderItem.builder()
                            .order(savedOrder)
                            .productVariant(variant)
                            .quantity(cartItem.getQuantity())
                            .unitPrice(variant.getProduct().getPrice())
                            .build();
                })
                .collect(Collectors.toList());

        savedOrder.setItems(orderItems);

        // 7. Vider le panier
        cartItemRepository.deleteAll(cart.getItems());

        return toResponse(orderRepository.save(savedOrder));
    }

    @Override
    public OrderResponse getOrderById(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access denied");
        }

        return toResponse(order);
    }

    @Override
    public Page<OrderResponse> getMyOrders(String userEmail, Pageable pageable) {
        return orderRepository.findByUserEmail(userEmail, pageable)
                .map(this::toResponse);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access denied");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException(
                    "Only PENDING orders can be cancelled");
        }

        // Remettre le stock
        order.getItems().forEach(item -> {
            ProductVariant variant = item.getProductVariant();
            variant.setStock(variant.getStock() + item.getQuantity());
            productVariantRepository.save(variant);
        });

        order.setStatus(Order.OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        try {
            order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }

        return toResponse(orderRepository.save(order));
    }

    // ===== HELPERS =====

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .couponCode(order.getCouponCode())
                .items(items)
                .customerEmail(order.getUser().getEmail())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        ProductVariant variant = item.getProductVariant();
        BigDecimal subtotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return OrderItemResponse.builder()
                .id(item.getId())
                .productName(variant.getProduct().getName())
                .size(variant.getSize())
                .color(variant.getColor())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(subtotal)
                .build();
    }
}