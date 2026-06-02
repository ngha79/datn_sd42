package com.base.service;

import com.base.dto.request.category.CreateCategoryRequest;
import com.base.dto.request.category.UpdateCategoryRequest;
import com.base.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    Page<Category> getCategories(Pageable pageable);
    Category save(CreateCategoryRequest category);
    Category update(Long id, UpdateCategoryRequest category);
    void delete(Long id);
    Category findById(Long id);
}
