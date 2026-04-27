package com.shopflow.service;

import com.shopflow.dto.request.AddressRequest;
import com.shopflow.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {
    List<AddressResponse> getAddresses(String userEmail);
    AddressResponse addAddress(String userEmail, AddressRequest request);
    void deleteAddress(String userEmail, Long addressId);
}