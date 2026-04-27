package com.shopflow.controller;

import com.shopflow.dto.request.AddressRequest;
import com.shopflow.dto.response.AddressResponse;
import com.shopflow.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getMyAddresses(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                addressService.getAddresses(userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(
                addressService.addAddress(userDetails.getUsername(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        addressService.deleteAddress(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}