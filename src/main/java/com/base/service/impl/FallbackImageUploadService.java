package com.base.service.impl;

import com.base.dto.request.ImageUploadMessage;
import com.base.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FallbackImageUploadService {

    private final CloudinaryService cloudinaryService;
    private final LocalStorageService localStorageService;
    private final ProductImageRepository imageRepository;

    @Async
    public void process(ImageUploadMessage message) {
        log.info("Fallback sync processing: imageId={}, action={}",
                message.getImageId(), message.getAction());
        try {
            switch (message.getAction()) {

                case CREATE -> {
                    String cloudUrl = cloudinaryService.uploadFromPath(message.getTempFilePath());
                    updateImageUrl(message.getImageId(), cloudUrl);
                    localStorageService.deleteTempFile(message.getTempFilePath());
                }

                case UPDATE -> {
                    cloudinaryService.deleteImage(message.getOldImageUrl());
                    String cloudUrl = cloudinaryService.uploadFromPath(message.getTempFilePath());
                    updateImageUrl(message.getImageId(), cloudUrl);
                    localStorageService.deleteTempFile(message.getTempFilePath());
                }

                case DELETE -> cloudinaryService.deleteImage(message.getOldImageUrl());
            }
        } catch (Exception e) {
            log.error("Fallback processing failed: imageId={}, error={}",
                    message.getImageId(), e.getMessage());
        }
    }

    @Transactional
    protected void updateImageUrl(Long imageId, String cloudUrl) {
        imageRepository.findById(imageId).ifPresent(img -> {
            img.setImageUrl(cloudUrl);
            imageRepository.save(img);
        });
    }
}
