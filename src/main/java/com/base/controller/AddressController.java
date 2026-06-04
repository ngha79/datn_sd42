package com.base.controller;

import com.base.dto.request.address.AddressRequest;
import com.base.dto.response.address.AddressResponse;
import com.base.service.AddressService;
import com.base.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class AddressController {

    private final AddressService addressService;

    private final SecurityUtils securityUtils;

    private Long getCurrentUserId() {
        return securityUtils.getCurrentUserId();
    }
    @PostMapping("/users/addresses")
    public ResponseEntity<AddressResponse> createAddress(
            @RequestBody @Valid AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressService.createAddress(getCurrentUserId(), request));
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressResponse> getById(
            @PathVariable Long addressId) {
        return ResponseEntity.ok(addressService.getAddressById(addressId));
    }

    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressResponse>> getByUser() {
        return ResponseEntity.ok(addressService.getAddressesByUser(getCurrentUserId()));
    }

    @GetMapping("/users/addresses/default")
    public ResponseEntity<AddressResponse> getDefault() {
        return ResponseEntity.ok(addressService.getDefaultAddress(getCurrentUserId()));
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long addressId,
            @RequestBody @Valid AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(addressId, request));
    }

    @PatchMapping("/users/addresses/{addressId}/default")
    public ResponseEntity<Void> setDefault(
            @PathVariable Long addressId) {
        addressService.setDefaultAddress(getCurrentUserId(), addressId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }
}
