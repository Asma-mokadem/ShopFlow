package com.shopflow.controller;

import com.shopflow.dto.request.CartItemRequest;
import com.shopflow.dto.response.CartResponse;
import com.shopflow.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // GET mon panier
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                cartService.getCart(userDetails.getUsername()));
    }

    // Ajouter un item
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(
                cartService.addItem(userDetails.getUsername(), request));
    }

    // Modifier la quantité
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(
                cartService.updateItem(userDetails.getUsername(), itemId, quantity));
    }

    // Supprimer un item
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(
                cartService.removeItem(userDetails.getUsername(), itemId));
    }

    // Vider le panier
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}