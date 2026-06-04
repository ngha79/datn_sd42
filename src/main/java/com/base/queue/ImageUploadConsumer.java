package com.base.queue;

import com.base.dto.request.ImageUploadBannerMessage;
import com.base.dto.request.ImageUploadPostMessage;
import com.base.dto.request.ImageUploadProductMessage;
import com.base.exception.BadRequestException;
import com.base.repository.BannerRepository;
import com.base.repository.PostRepository;
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
    private final BannerRepository bannerRepository;
    private final PostRepository postRepository;

    @RabbitListener(queues = "${rabbitmq.queue.image-upload}")
    public void handleImageUpload(ImageUploadProductMessage message) {
        log.info("Processing: imageId={}, action={}", message.getImageId(), message.getAction());
        try {
            switch (message.getAction()) {

                case CREATE -> {
                    String cloudUrl = cloudinaryService.uploadFromPath(message.getTempFilePath());

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

    @RabbitListener(queues = "${rabbitmq.queue.image-upload}")
    public void handleImageUpload(ImageUploadBannerMessage message) {
        log.info("Processing: bannerId={}, action={}", message.getBannerId(), message.getAction());
        try {
            switch (message.getAction()) {

                case CREATE -> {
                    String cloudUrl = cloudinaryService.uploadFromPath(message.getTempFilePath());

                    bannerRepository.findById(message.getBannerId()).ifPresent(img -> {
                        img.setImageUrl(cloudUrl);
                        bannerRepository.save(img);
                    });

                    localStorageService.deleteTempFile(message.getTempFilePath());
                    log.info("Image uploaded: bannerId={}, url={}", message.getBannerId(), cloudUrl);
                }

                case UPDATE -> {
                    if (message.getOldImageUrl() != null) {
                        cloudinaryService.deleteImage(message.getOldImageUrl());
                    }
                    String cloudUrl = cloudinaryService.uploadFromPath(message.getTempFilePath());

                    bannerRepository.findById(message.getBannerId()).ifPresent(img -> {
                        img.setImageUrl(cloudUrl);
                        bannerRepository.save(img);
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
            log.error("Failed to process image: bannerId={}, error={}",
                    message.getBannerId(), e.getMessage());
            throw new BadRequestException("RabbitMQ retry"); // RabbitMQ retry
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.image-upload}")
    public void handleImageUpload(ImageUploadPostMessage message) {
        log.info("Processing: postId={}, action={}", message.getPostId(), message.getAction());
        try {
            switch (message.getAction()) {

                case CREATE -> {
                    String cloudUrl = cloudinaryService.uploadFromPath(message.getTempFilePath());

                    postRepository.findById(message.getPostId()).ifPresent(img -> {
                        img.setThumbnail(cloudUrl);
                        postRepository.save(img);
                    });

                    localStorageService.deleteTempFile(message.getTempFilePath());
                    log.info("Image uploaded: postId={}, url={}", message.getPostId(), cloudUrl);
                }

                case UPDATE -> {
                    if (message.getOldImageUrl() != null) {
                        cloudinaryService.deleteImage(message.getOldImageUrl());
                    }
                    String cloudUrl = cloudinaryService.uploadFromPath(message.getTempFilePath());

                    postRepository.findById(message.getPostId()).ifPresent(img -> {
                        img.setThumbnail(cloudUrl);
                        postRepository.save(img);
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
            log.error("Failed to process image: postId={}, error={}",
                    message.getPostId(), e.getMessage());
            throw new BadRequestException("RabbitMQ retry"); // RabbitMQ retry
        }
    }
}