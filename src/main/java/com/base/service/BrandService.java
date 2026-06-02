package com.base.service;

import com.base.dto.request.brand.CreateBrandRequest;
import com.base.dto.request.brand.UpdateBrandRequest;
import com.base.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BrandService {
    Page<Brand> getCategories(Pageable pageable);
    Brand save(CreateBrandRequest brand);
    Brand update(Long id, UpdateBrandRequest brand);
    void delete(Long id);
    Brand findById(Long id);
}