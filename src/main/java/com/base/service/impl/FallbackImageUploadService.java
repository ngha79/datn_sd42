package com.base.service.impl;

import com.base.dto.request.ImageUploadBannerMessage;
import com.base.dto.request.ImageUploadChatMessage;
import com.base.dto.request.ImageUploadPostMessage;
import com.base.dto.request.ImageUploadProductMessage;
import com.base.repository.BannerRepository;
import com.base.repository.MessageRepository;
import com.base.repository.PostRepository;
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
    private final BannerRepository bannerRepository;
    private final PostRepository postRepository;
    private final MessageRepository messageRepository;

    @Async
    public void process(ImageUploadProductMessage message) {
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

    @Async
    public void process(ImageUploadBannerMessage message) {
        log.info("Fallback sync processing: bannerId={}, action={}",
                message.getBannerId(), message.getAction());
        try {
            switch (message.getAction()) {

                case CREATE -> {
                    String cloudUrl = cloudinaryService.uploadFromPath(message.getTempFilePath());
                    updateImageBannerUrl(message.getBannerId(), cloudUrl);
                    localStorageService.deleteTempFile(message.getTempFilePath());
                }

                case UPDATE -> {
                    cloudinaryService.deleteImage(message.getOldImageUrl());
                    String cloudUrl = cloudinaryService.uploadFromPath(message.getTempFilePath());
                    updateImageBannerUrl(message.getBannerId(), cloudUrl);
                    localStorageService.deleteTempFile(message.getTempFilePath());
                }

                case DELETE -> cloudinaryService.deleteImage(message.getOldImageUrl());
            }
        } catch (Exception e) {
            log.error("Fallback processing failed: bannerId={}, error={}",
                    message.getBannerId(), e.getMessage());
        }
    }

    @Async
    public void process(ImageUploadPostMessage message) {
        log.info("Fallback sync processing: bannerId={}, action={}",
                message.getPostId(), message.getAction());
        try {
            switch (message.getAction()) {

                case CREATE -> {
                    String cloudUrl = cloudinaryService.uploadFromPath(message.getTempFilePath());
                    updateImagePostUrl(message.getPostId(), cloudUrl);
                    localStorageService.deleteTempFile(message.getTempFilePath());
                }

                case UPDATE -> {
                    cloudinaryService.deleteImage(message.getOldImageUrl());
                    String cloudUrl = cloudinaryService.uploadFromPath(message.getTempFilePath());
                    updateImagePostUrl(message.getPostId(), cloudUrl);
                    localStorageService.deleteTempFile(message.getTempFilePath());
                }

                case DELETE -> cloudinaryService.deleteImage(message.getOldImageUrl());
            }
        } catch (Exception e) {
            log.error("Fallback processing failed: postId={}, error={}",
                    message.getPostId(), e.getMessage());
        }
    }

    @Async
    public void process(ImageUploadChatMessage message) {
        log.info("Fallback sync processing: messageId={}, action={}",
                message.getMessageId(), message.getAction());
        try {
            switch (message.getAction()) {

                case CREATE -> {
                    String cloudUrl = cloudinaryService.uploadFromPath(message.getTempFilePath());
                    updateImageMessageUrl(message.getMessageId(), cloudUrl);
                    localStorageService.deleteTempFile(message.getTempFilePath());
                }

                case UPDATE -> {
                    cloudinaryService.deleteImage(message.getOldImageUrl());
                    String cloudUrl = cloudinaryService.uploadFromPath(message.getTempFilePath());
                    updateImageMessageUrl(message.getMessageId(), cloudUrl);
                    localStorageService.deleteTempFile(message.getTempFilePath());
                }

                case DELETE -> cloudinaryService.deleteImage(message.getOldImageUrl());
            }
        } catch (Exception e) {
            log.error("Fallback processing failed: postId={}, error={}",
                    message.getMessageId(), e.getMessage());
        }
    }

    @Transactional
    protected void updateImageUrl(Long imageId, String cloudUrl) {
        imageRepository.findById(imageId).ifPresent(img -> {
            img.setImageUrl(cloudUrl);
            imageRepository.save(img);
        });
    }

    @Transactional
    protected void updateImageBannerUrl(Long bannerId, String cloudUrl) {
        bannerRepository.findById(bannerId).ifPresent(img -> {
            img.setImageUrl(cloudUrl);
            bannerRepository.save(img);
        });
    }

    @Transactional
    protected void updateImageMessageUrl(Long bannerId, String cloudUrl) {
        messageRepository.findById(bannerId).ifPresent(img -> {
            img.setImageUrl(cloudUrl);
            messageRepository.save(img);
        });
    }

    @Transactional
    protected void updateImagePostUrl(Long postId, String cloudUrl) {
        postRepository.findById(postId).ifPresent(img -> {
            img.setThumbnail(cloudUrl);
            postRepository.save(img);
        });
    }
}
