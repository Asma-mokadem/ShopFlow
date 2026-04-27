package com.shopflow.dto.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private Long id;
    private String street;
    private String city;
    private String zipCode;
    private String country;
    private boolean isDefault;
}