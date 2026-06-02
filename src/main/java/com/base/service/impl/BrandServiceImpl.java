package com.base.service.impl;

import com.base.dto.request.brand.CreateBrandRequest;
import com.base.dto.request.brand.UpdateBrandRequest;
import com.base.entity.Brand;
import com.base.exception.BadRequestException;
import com.base.exception.ResourceNotFoundException;
import com.base.repository.BrandRepository;
import com.base.repository.ProductRepository;
import com.base.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<Brand> getCategories(Pageable pageable) {
        return brandRepository.findAll(pageable);
    }

    @Override
    public Brand save(CreateBrandRequest request) {

        if (brandRepository.existsByBrandName(request.getBrandName())) {
            throw new BadRequestException("Tên thương hiệu đã tồn tại");
        }

        Brand brand = modelMapper.map(request, Brand.class);

        return brandRepository.save(brand);
    }

    @Override
    public Brand update(Long id, UpdateBrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Thương hiệu", "id", id));

        if (brandRepository.existsByBrandNameAndBrandIdNot(
                request.getBrandName(), id)) {
            throw new BadRequestException("Tên thương hiệu đã tồn tại");
        }

        modelMapper.map(request, brand);

        return brandRepository.save(brand);
    }

    @Override
    public void delete(Long id) {
        if (productRepository.existsByBrand_BrandId(id)) {
            throw new BadRequestException(
                    "Không thể xóa thương hiệu vì đang có sản phẩm thuộc thương hiệu này"
            );
        }
        brandRepository.deleteById(id);
    }

    @Override
    public Brand findById(Long id) {
        return brandRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Thương hiệu","id",id));
    }
}
