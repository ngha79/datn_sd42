package com.base.dto.request.address;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {

    @NotBlank
    private String consigneeName;

    @NotBlank
    private String phone;

    @NotBlank
    private String province;

    @NotBlank
    private String district;

    @NotBlank
    private String ward;

    @NotBlank
    private String streetAddress;

    private Boolean isDefault;
}