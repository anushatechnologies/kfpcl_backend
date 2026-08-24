package com.kfpcl.serviceImpl;

import com.kfpcl.dto.ImageUploadResponseDto;
import com.kfpcl.exception.InvalidRequestException;
import com.kfpcl.service.ImageUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ImageUploadServiceImpl implements ImageUploadService {

    @Value("${file.upload.dir:uploads/catalog/}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp", "gif");

    @Override
    public ImageUploadResponseDto uploadCatalogImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("No image file provided for upload");
        }

        String rawName = file.getOriginalFilename();
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNullElse(rawName, "image.jpg"));
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidRequestException("Invalid image file format. Allowed extensions: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        try {
            Path targetDir = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String uniqueFileName = "cat_img_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;
            Path targetPath = targetDir.resolve(uniqueFileName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/" + uniqueFileName;

            return ImageUploadResponseDto.builder()
                    .fileName(uniqueFileName)
                    .fileUrl(fileUrl)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .uploadedAt(LocalDateTime.now())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded image: " + e.getMessage(), e);
        }
    }
}
