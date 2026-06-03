package com.base.service.impl;

import com.base.dto.request.product.ProductRequest;
import com.base.dto.response.product.ProductResponse;
import com.base.entity.Brand;
import com.base.entity.Category;
import com.base.entity.Product;
import com.base.exception.BadRequestException;
import com.base.exception.ResourceAlreadyExistsException;
import com.base.exception.ResourceNotFoundException;
import com.base.repository.BrandRepository;
import com.base.repository.CategoryRepository;
import com.base.repository.ProductRepository;
import com.base.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository      productRepository;
    private final CategoryRepository     categoryRepository;
    private final BrandRepository        brandRepository;
    private final ModelMapper            modelMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {

        if (productRepository.existsByProductNameAndDeletedFalse(request.getProductName())) {
            throw new ResourceAlreadyExistsException(
                    "Tên sản phẩm đã tồn tại: " + request.getProductName());
        }

        Product product = new Product();

        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());

        product.setCategory(resolveCategory(request.getCategoryId()));
        product.setBrand(resolveBrand(request.getBrandId()));

        product.setProductId(null);
        product.setAverageRating(BigDecimal.ZERO);
        product.setReviewCount(0);
        product.setDeleted(false);

        product = productRepository.save(product);

        log.info("Created product id={}", product.getProductId());

        return toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return toResponse(findActiveById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository
                .findAllByDeletedFalse(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> filterProducts(Long categoryId, Long brandId,
                                                BigDecimal minPrice, BigDecimal maxPrice,
                                                Product.ProductStatus status, String keyword,
                                                Pageable pageable) {
        return productRepository
                .filterProducts(categoryId, brandId, minPrice, maxPrice,
                        status, keyword, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getTopRated(int limit) {
        return productRepository
                .findTop10ByDeletedFalseOrderByAverageRatingDesc()
                .stream()
                .limit(limit)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getDeleted(Pageable pageable) {
        return productRepository
                .findAllByDeletedTrue(pageable)
                .map(this::toResponse);
    }


    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {

        Product product = findActiveById(id);

        if (request.getProductName() != null) {
            product.setProductName(request.getProductName());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getBasePrice() != null) {
            product.setBasePrice(request.getBasePrice());
        }

        if (request.getCategoryId() != null) {
            product.setCategory(resolveCategory(request.getCategoryId()));
        }

        if (request.getBrandId() != null) {
            product.setBrand(resolveBrand(request.getBrandId()));
        }

        product = productRepository.save(product);

        return toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse changeStatus(Long id, Product.ProductStatus status) {
        Product product = findActiveById(id);
        product.setStatus(status);
        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void restoreProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm: " + id));

        if (!product.isDeleted()) {
            throw new BadRequestException("Sản phẩm chưa bị xóa: " + id);
        }

        product.setDeleted(false);
        productRepository.save(product);
        log.info("Restored product id={}", id);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findActiveById(id);
        product.setDeleted(true);
        productRepository.save(product);
        log.info("Soft-deleted product id={}", id);
    }

    @Override
    @Transactional
    public void bulkDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;

        List<Product> products = productRepository.findAllById(ids)
                .stream()
                .filter(p -> !p.isDeleted())
                .toList();

        products.forEach(p -> p.setDeleted(true));
        productRepository.saveAll(products);
        log.info("Bulk soft-deleted {} products", products.size());
    }

    @Override
    @Transactional
    public void bulkUpdateStatus(List<Long> ids, Product.ProductStatus status) {
        if (ids == null || ids.isEmpty()) return;

        List<Product> products = productRepository.findAllById(ids)
                .stream()
                .filter(p -> !p.isDeleted())
                .toList();

        products.forEach(p -> p.setStatus(status));
        productRepository.saveAll(products);
        log.info("Bulk updated status={} for {} products", status, products.size());
    }

    @Override
    @Transactional
    public void updateRating(Long productId, BigDecimal newRating) {
        if (newRating.compareTo(BigDecimal.ONE) < 0
                || newRating.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Product product = findActiveById(productId);

        int    oldCount = product.getReviewCount();
        BigDecimal oldSum = product.getAverageRating()
                .multiply(BigDecimal.valueOf(oldCount));

        int    newCount = oldCount + 1;
        BigDecimal newAvg = oldSum.add(newRating)
                .divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);

        product.setAverageRating(newAvg);
        product.setReviewCount(newCount);
        productRepository.save(product);

        log.info("Updated rating for product id={}: avg={}, count={}",
                productId, newAvg, newCount);
    }


    private Product findActiveById(Long id) {
        return productRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm: " + id));
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục: " + categoryId));
    }

    private Brand resolveBrand(Long brandId) {
        if (brandId == null) return null;
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu: " + brandId));
    }

    private boolean isValidFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private ProductResponse toResponse(Product product) {
        return modelMapper.map(product, ProductResponse.class);
    }
}