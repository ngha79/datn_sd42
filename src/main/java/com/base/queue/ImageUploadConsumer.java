package com.base.queue;

import com.base.dto.request.ImageUploadMessage;
import com.base.exception.BadRequestException;
import com.base.repository.ProductImageRepository;
import com.base.service.impl.CloudinaryService;
import com.base.service.impl.LocalStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImageUploadConsumer {

    private final CloudinaryService cloudinaryService;
    private final LocalStorageService localStorageService;
    private final ProductImageRepository imageRepository;

    @RabbitListener(queues = "${rabbitmq.queue.image-upload}")
    public void handleImageUpload(ImageUploadMessage message) {
        log.info("Processing: imageId={}, action={}", message.getImageId(), message.getAction());
        try {
            switch (message.getAction()) {

                case CREATE -> {
                    String cloudUrl = cloudinaryService.uploadFromPath(message.getTempFilePath());

                    // Cập nhật URL thật vào Varriant Image
                    imageRepository.findById(message.getImageId()).ifPresent(img -> {
                        img.setImageUrl(cloudUrl);
                        imageRepository.save(img);
                    });

                    localStorageService.deleteTempFile(message.getTempFilePath());
                    log.info("Image uploaded: imageId={}, url={}", message.getImageId(), cloudUrl);
                }

                case UPDATE -> {
                    if (message.getOldImageUrl() != null) {
                        cloudinaryService.deleteImage(message.getOldImageUrl());
                    }
                    String cloudUrl = cloudinaryService.uploadFromPath(message.getTempFilePath());

                    imageRepository.findById(message.getImageId()).ifPresent(img -> {
                        img.setImageUrl(cloudUrl);
                        imageRepository.save(img);
                    });

                    localStorageService.deleteTempFile(message.getTempFilePath());
                }

                case DELETE -> {
                    if (message.getOldImageUrl() != null) {
                        cloudinaryService.deleteImage(message.getOldImageUrl());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to process image: imageId={}, error={}",
                    message.getImageId(), e.getMessage());
            throw new BadRequestException("RabbitMQ retry"); // RabbitMQ retry
        }
    }
}