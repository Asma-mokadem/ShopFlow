package com.shopflow.service.impl;

import com.shopflow.dto.request.CartItemRequest;
import com.shopflow.dto.response.CartItemResponse;
import com.shopflow.dto.response.CartResponse;
import com.shopflow.entity.*;
import com.shopflow.repository.*;
import com.shopflow.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public CartResponse getCart(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        return toResponse(cart);
    }

    @Override
    public CartResponse addItem(String userEmail, CartItemRequest request) {
        Cart cart = getOrCreateCart(userEmail);

        ProductVariant variant = productVariantRepository
                .findById(request.getProductVariantId())
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        // Vérifier le stock
        if (variant.getStock() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock. Available: " + variant.getStock());
        }

        // Vérifier si l'item existe déjà dans le panier
        cartItemRepository.findByCartIdAndProductVariantId(
                        cart.getId(), variant.getId())
                .ifPresentOrElse(
                        existingItem -> {
                            int newQty = existingItem.getQuantity() + request.getQuantity();
                            if (variant.getStock() < newQty) {
                                throw new RuntimeException("Insufficient stock");
                            }
                            existingItem.setQuantity(newQty);
                            cartItemRepository.save(existingItem);
                        },
                        () -> {
                            CartItem newItem = CartItem.builder()
                                    .cart(cart)
                                    .productVariant(variant)
                                    .quantity(request.getQuantity())
                                    .build();
                            cartItemRepository.save(newItem);
                        }
                );

        // Recharger le panier
        Cart updatedCart = cartRepository.findByUserEmail(userEmail).orElseThrow();
        return toResponse(updatedCart);
    }

    @Override
    public CartResponse updateItem(String userEmail, Long itemId, Integer quantity) {
        Cart cart = getOrCreateCart(userEmail);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Vérifier que l'item appartient au panier
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Item does not belong to your cart");
        }

        // Vérifier stock
        if (item.getProductVariant().getStock() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        Cart updatedCart = cartRepository.findByUserEmail(userEmail).orElseThrow();
        return toResponse(updatedCart);
    }

    @Override
    public CartResponse removeItem(String userEmail, Long itemId) {
        Cart cart = getOrCreateCart(userEmail);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Item does not belong to your cart");
        }

        cartItemRepository.delete(item);

        Cart updatedCart = cartRepository.findByUserEmail(userEmail).orElseThrow();
        return toResponse(updatedCart);
    }

    @Override
    public void clearCart(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        cartItemRepository.deleteAll(cart.getItems());
    }

    // ===== HELPERS =====

    private Cart getOrCreateCart(String userEmail) {
        return cartRepository.findByUserEmail(userEmail)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(userEmail)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .total(total)
                .itemCount(items.size())
                .build();
    }

    private CartItemResponse toItemResponse(CartItem item) {
        ProductVariant variant = item.getProductVariant();
        BigDecimal unitPrice = variant.getProduct().getPrice();
        BigDecimal subtotal = unitPrice.multiply(
                BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .id(item.getId())
                .productVariantId(variant.getId())
                .productName(variant.getProduct().getName())
                .size(variant.getSize())
                .color(variant.getColor())
                .quantity(item.getQuantity())
                .unitPrice(unitPrice)
                .subtotal(subtotal)
                .build();
    }
}