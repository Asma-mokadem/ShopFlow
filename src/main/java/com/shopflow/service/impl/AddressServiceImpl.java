package com.shopflow.service.impl;

import com.shopflow.dto.request.AddressRequest;
import com.shopflow.dto.response.AddressResponse;
import com.shopflow.entity.Address;
import com.shopflow.entity.User;
import com.shopflow.repository.AddressRepository;
import com.shopflow.repository.UserRepository;
import com.shopflow.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public List<AddressResponse> getAddresses(String userEmail) {
        return addressRepository.findByUserEmail(userEmail)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AddressResponse addAddress(String userEmail, AddressRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = Address.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .isDefault(request.isDefault())
                .user(user)
                .build();

        return toResponse(addressRepository.save(address));
    }

    @Override
    public void deleteAddress(String userEmail, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access denied");
        }

        addressRepository.delete(address);
    }

    private AddressResponse toResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .isDefault(address.isDefault())
                .build();
    }
}