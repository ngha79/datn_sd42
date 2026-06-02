package com.base.service.impl;

import com.base.dto.request.category.CreateCategoryRequest;
import com.base.dto.request.category.UpdateCategoryRequest;
import com.base.entity.Category;
import com.base.exception.BadRequestException;
import com.base.exception.ResourceNotFoundException;
import com.base.repository.CategoryRepository;
import com.base.repository.ProductRepository;
import com.base.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<Category> getCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public Category save(CreateCategoryRequest request) {

        if (categoryRepository.existsByCategoryName(request.getCategoryName())) {
            throw new BadRequestException("Tên danh mục đã tồn tại");
        }

        Category category = modelMapper.map(request, Category.class);

        return categoryRepository.save(category);
    }

    @Override
    public Category update(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Danh mục", "id", id));

        if (categoryRepository.existsByCategoryNameAndCategoryIdNot(
                request.getCategoryName(), id)) {
            throw new BadRequestException("Tên danh mục đã tồn tại");
        }

        modelMapper.map(request, category);

        return categoryRepository.save(category);
    }

    @Override
    public void delete(Long id) {
        if (productRepository.existsByCategory_CategoryId(id)) {
            throw new BadRequestException(
                    "Không thể xóa danh mục vì đang có sản phẩm thuộc danh mục này"
            );
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() ->
                        new ResourceNotFoundException("Danh mục","id",id));
    }
}
