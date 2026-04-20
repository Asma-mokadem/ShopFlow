package com.shopflow.service;

import com.shopflow.dto.request.OrderRequest;
import com.shopflow.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse createOrder(String userEmail, OrderRequest request);
    OrderResponse getOrderById(Long orderId, String userEmail);
    Page<OrderResponse> getMyOrders(String userEmail, Pageable pageable);
    OrderResponse cancelOrder(Long orderId, String userEmail);

    // Admin
    Page<OrderResponse> getAllOrders(Pageable pageable);
    OrderResponse updateOrderStatus(Long orderId, String status);
}