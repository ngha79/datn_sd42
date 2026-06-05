package com.base.service.impl;

import com.base.dto.request.ImageUploadProductMessage;
import com.base.dto.request.product.ProductVariantRequest;
import com.base.dto.response.product.ProductImageResponse;
import com.base.dto.response.product.ProductVariantResponse;
import com.base.entity.Product;
import com.base.entity.ProductImage;
import com.base.entity.ProductVariant;
import com.base.exception.ResourceAlreadyExistsException;
import com.base.exception.ResourceNotFoundException;
import com.base.queue.ImageUploadProducer;
import com.base.repository.ProductImageRepository;
import com.base.repository.ProductRepository;
import com.base.repository.ProductVariantRepository;
import com.base.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final LocalStorageService localStorageService;
    private final ImageUploadProducer imageUploadProducer;
    private final ModelMapper modelMapper;

    @Override
    public ProductVariantResponse addVariant(Long productId, ProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        if (variantRepository.existsBySku(request.getSku())) {
            throw new ResourceAlreadyExistsException("SKU already exists: " + request.getSku());
        }

        ProductVariant variant = modelMapper.map(request, ProductVariant.class);
        variant.setProduct(product);

        return modelMapper.map(variantRepository.save(variant), ProductVariantResponse.class);
    }

    @Override
    public ProductVariantResponse updateVariant(Long variantId, ProductVariantRequest request) {
        ProductVariant variant = findVariantById(variantId);
        modelMapper.map(request, variant);
        return modelMapper.map(variantRepository.save(variant), ProductVariantResponse.class);
    }

    @Override
    public void deleteVariant(Long variantId) {
        ProductVariant variant = findVariantById(variantId);

        // Xóa tất cả ảnh Cloudinary của variant bất đồng bộ
        variant.getImages().forEach(img -> {
            if (img.getImageUrl() != null && img.getImageUrl().contains("cloudinary")) {
                imageUploadProducer.sendUploadProductMessage(
                        ImageUploadProductMessage.builder()
                                .imageId(img.getImageId())
                                .oldImageUrl(img.getImageUrl())
                                .action(ImageUploadProductMessage.ActionType.DELETE)
                                .build()
                );
            }
        });

        variantRepository.delete(variant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantResponse> getVariantsByProduct(Long productId) {
        return variantRepository.findByProduct_ProductId(productId)
                .stream()
                .map(v -> modelMapper.map(v, ProductVariantResponse.class))
                .toList();
    }

    @Override
    @Transactional
    public ProductImageResponse addImage(Long variantId, MultipartFile file, boolean isThumbnail) {
        ProductVariant variant = findVariantById(variantId);

        // Nếu set làm thumbnail → reset tất cả thumbnail cũ trước
        if (isThumbnail) {
            List<ProductImage> oldThumbnails =
                    imageRepository.findByVariantVariantIdAndThumbnailTrue(variantId);
            if (!oldThumbnails.isEmpty()) {
                oldThumbnails.forEach(img -> img.setThumbnail(false));
                imageRepository.saveAll(oldThumbnails);
            }
        }

        String tempPath = localStorageService.saveTempFile(file);
        String tempUrl  = localStorageService.getTempUrl(tempPath);

        ProductImage image = ProductImage.builder()
                .variant(variant)
                .imageUrl(tempUrl)
                .thumbnail(isThumbnail)
                .build();
        image = imageRepository.save(image);

        imageUploadProducer.sendUploadProductMessage(
                ImageUploadProductMessage.builder()
                        .imageId(image.getImageId())
                        .variantId(variantId)
                        .tempFilePath(tempPath)
                        .action(ImageUploadProductMessage.ActionType.CREATE)
                        .build()
        );

        return modelMapper.map(image, ProductImageResponse.class);
    }

    @Override
    public void deleteImage(Long imageId) {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));

        imageRepository.delete(image);

        // Xóa Cloudinary bất đồng bộ
        if (image.getImageUrl() != null && image.getImageUrl().contains("cloudinary")) {
            imageUploadProducer.sendUploadProductMessage(
                    ImageUploadProductMessage.builder()
                            .imageId(imageId)
                            .oldImageUrl(image.getImageUrl())
                            .action(ImageUploadProductMessage.ActionType.DELETE)
                            .build()
            );
        }
    }

    @Override
    @Transactional
    public ProductImageResponse setThumbnail(Long imageId) {
        ProductImage newThumbnail = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));

        Long variantId = newThumbnail.getVariant().getVariantId();

        // reset all ảnh đang là thumbnail của variant này
        List<ProductImage> currentThumbnails =
                imageRepository.findByVariantVariantIdAndThumbnailTrue(variantId);

        if (!currentThumbnails.isEmpty()) {
            currentThumbnails.forEach(img -> img.setThumbnail(false));
            imageRepository.saveAll(currentThumbnails);
            log.info("Reset {} old thumbnail(s) for variantId={}", currentThumbnails.size(), variantId);
        }

        // Set thumbnail mới
        newThumbnail.setThumbnail(true);
        ProductImage saved = imageRepository.save(newThumbnail);
        log.info("Set thumbnail imageId={} for variantId={}", imageId, variantId);

        return modelMapper.map(saved, ProductImageResponse.class);
    }

    private ProductVariant findVariantById(Long id) {
        return variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + id));
    }
}
