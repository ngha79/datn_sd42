package com.base.service;

import com.base.dto.request.address.AddressRequest;
import com.base.dto.response.address.AddressResponse;

import java.util.List;

public interface AddressService {

    // Thêm địa chỉ mới
    AddressResponse createAddress(Long userId, AddressRequest request);

    // Cập nhật địa chỉ
    AddressResponse updateAddress(Long addressId, AddressRequest request);

    // Xóa địa chỉ
    void deleteAddress(Long addressId);

    // Lấy chi tiết địa chỉ
    AddressResponse getAddressById(Long addressId);

    // Lấy tất cả địa chỉ của user
    List<AddressResponse> getAddressesByUser(Long userId);

    // Lấy địa chỉ mặc định
    AddressResponse getDefaultAddress(Long userId);

    // Đặt địa chỉ mặc định
    void setDefaultAddress(Long userId, Long addressId);
}