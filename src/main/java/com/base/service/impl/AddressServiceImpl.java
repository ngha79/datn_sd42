package com.base.service.impl;

import com.base.dto.request.address.AddressRequest;
import com.base.dto.response.address.AddressResponse;
import com.base.entity.Address;
import com.base.entity.User;
import com.base.exception.BadRequestException;
import com.base.exception.ResourceNotFoundException;
import com.base.repository.AddressRepository;
import com.base.repository.UserRepository;
import com.base.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository    userRepository;
    private final ModelMapper       modelMapper;

    @Override
    public AddressResponse createAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            resetDefaultAddress(userId);
        }

        boolean hasNoAddress = addressRepository.countByUserUserId(userId) == 0;

        Address address = modelMapper.map(request, Address.class);
        address.setAddressId(null);
        address.setUser(user);
        address.setIsDefault(hasNoAddress || Boolean.TRUE.equals(request.getIsDefault()));

        Address saved = addressRepository.save(address);
        log.info("Created address id={} for userId={}", saved.getAddressId(), userId);

        return toResponse(saved);
    }

    @Override
    public AddressResponse updateAddress(Long addressId, AddressRequest request) {
        Address address = findById(addressId);

        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.getIsDefault()) {
            resetDefaultAddress(address.getUser().getUserId());
            address.setIsDefault(true);
        }

        modelMapper.map(request, address);
        Address saved = addressRepository.save(address);
        log.info("Updated address id={}", addressId);

        return toResponse(saved);
    }

    @Override
    public void deleteAddress(Long addressId) {
        Address address = findById(addressId);
        Long userId = address.getUser().getUserId();

        addressRepository.delete(address);
        log.info("Deleted address id={}", addressId);

        if (address.getIsDefault()) {
            addressRepository.findFirstByUserUserIdOrderByAddressIdAsc(userId)
                    .ifPresent(next -> {
                        next.setIsDefault(true);
                        addressRepository.save(next);
                        log.info("Auto-set new default address id={} for userId={}",
                                next.getAddressId(), userId);
                    });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long addressId) {
        return toResponse(findById(addressId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddressesByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Người dùng không tồn tại: " + userId);
        }
        return addressRepository.findByUserUserIdOrderByIsDefaultDescAddressIdAsc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getDefaultAddress(Long userId) {
        return addressRepository.findByUserUserIdAndIsDefaultTrue(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Người dùng không có địa chỉ mặc định: " + userId));
    }

    @Override
    public void setDefaultAddress(Long userId, Long addressId) {
        Address address = findById(addressId);

        if (!address.getUser().getUserId().equals(userId)) {
            throw new BadRequestException(
                    "Address id=" + addressId + " does not belong to userId=" + userId);
        }

        if (address.getIsDefault()) {
            log.info("Address id={} is already the default", addressId);
            return;
        }

        resetDefaultAddress(userId);
        address.setIsDefault(true);
        addressRepository.save(address);
        log.info("Set default address id={} for userId={}", addressId, userId);
    }



    private Address findById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ: " + addressId));
    }

    private void resetDefaultAddress(Long userId) {
        addressRepository.findByUserUserIdAndIsDefaultTrue(userId)
                .ifPresent(addr -> {
                    addr.setIsDefault(false);
                    addressRepository.save(addr);
                });
    }

    private AddressResponse toResponse(Address address) {
        return modelMapper.map(address, AddressResponse.class);
    }
}
