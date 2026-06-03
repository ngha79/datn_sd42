package com.base.service.impl;

import com.base.exception.BadRequestException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder}")
    private String folder;

    // Upload từ MultipartFile
    public String uploadImage(MultipartFile file) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image",
                            "use_filename", true,
                            "unique_filename", true
                    )
            );
            String url = result.get("secure_url").toString();
            log.info("Uploaded image to Cloudinary: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Upload image failed: {}", e.getMessage());
            throw new BadRequestException("Cannot upload image to Cloudinary");
        }
    }

    // Upload từ file path trên disk
    public String uploadFromPath(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new BadRequestException("Temp file not found: " + filePath);
            }

            Map<?, ?> result = cloudinary.uploader().upload(
                    file,
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image",
                            "use_filename", true,
                            "unique_filename", true
                    )
            );
            String url = result.get("secure_url").toString();
            log.info("Uploaded from path to Cloudinary: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Upload from path failed: {}", e.getMessage());
            throw new BadRequestException("Cannot upload file to Cloudinary");
        }
    }

    // Update: xóa ảnh cũ > upload ảnh mới (sync)
    public String updateImage(MultipartFile file, String oldImageUrl) {
        deleteImage(oldImageUrl);
        return uploadImage(file);
    }

    // Xóa ảnh theo URL
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        // Bỏ qua URL tạm (localhost) — chỉ xóa Cloudinary URL
        if (!imageUrl.contains("cloudinary.com")) {
            log.debug("Skipping delete for non-Cloudinary URL: {}", imageUrl);
            return;
        }

        try {
            String publicId = extractPublicId(imageUrl);
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted image from Cloudinary: publicId={}, result={}", publicId, result.get("result"));
        } catch (IOException e) {
            log.error("Delete image failed for url={}: {}", imageUrl, e.getMessage());
        }
    }


    private String extractPublicId(String imageUrl) {
        try {
            String marker = "/upload/";
            int uploadIndex = imageUrl.indexOf(marker);
            if (uploadIndex == -1) {
                throw new RuntimeException("Invalid Cloudinary URL: " + imageUrl);
            }

            String afterUpload = imageUrl.substring(uploadIndex + marker.length());

            if (afterUpload.matches("v\\d+/.*")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
            }

            int dotIndex = afterUpload.lastIndexOf('.');
            if (dotIndex != -1) {
                afterUpload = afterUpload.substring(0, dotIndex);
            }

            return afterUpload;

        } catch (Exception e) {
            log.error("Cannot extract public_id from URL: {}", imageUrl);
            throw new BadRequestException("Invalid Cloudinary URL format");
        }
    }
}