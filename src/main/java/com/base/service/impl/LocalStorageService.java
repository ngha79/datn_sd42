package com.base.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class LocalStorageService {

    @Value("${app.upload.temp-dir}")
    private String tempDir;

    @Value("${app.upload.base-url}")
    private String baseUrl;

    /**
     * Lưu file tạm vào disk, trả về đường dẫn tuyệt đối
     */
    public String saveTempFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(tempDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Tạo tên file unique tránh trùng lặp
            String ext = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + "." + ext;
            Path filePath = uploadPath.resolve(filename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Saved temp file: {}", filePath.toAbsolutePath());
            return filePath.toAbsolutePath().toString();

        } catch (IOException e) {
            log.error("Failed to save temp file: {}", e.getMessage());
            throw new RuntimeException("Cannot save temp file", e);
        }
    }

    /**
     * Tạo URL tạm để client có thể preview ngay
     */
    public String getTempUrl(String absolutePath) {
        String filename = Paths.get(absolutePath).getFileName().toString();
        return baseUrl + "/" + tempDir + "/" + filename;
    }

    /**
     * Xóa file tạm sau khi đã upload Cloudinary thành công
     */
    public void deleteTempFile(String absolutePath) {
        try {
            Path path = Paths.get(absolutePath);
            Files.deleteIfExists(path);
            log.info("Deleted temp file: {}", absolutePath);
        } catch (IOException e) {
            log.warn("Cannot delete temp file: {}", absolutePath);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
