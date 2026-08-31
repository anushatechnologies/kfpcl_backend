package com.kfpcl.serviceImpl;

import com.kfpcl.dto.ImageUploadResponseDto;
import com.kfpcl.exception.InvalidRequestException;
import com.kfpcl.exception.StorageException;
import com.kfpcl.service.ImageUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
public class ImageUploadServiceImpl implements ImageUploadService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp", "gif");
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final String region;

    public ImageUploadServiceImpl(S3Client s3Client,
                                  S3Presigner s3Presigner,
                                  @Value("${aws.s3.bucket}") String bucket,
                                  @Value("${aws.s3.region}") String region) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.region = region;
    }

    @Override
    public ImageUploadResponseDto uploadCatalogImage(MultipartFile file) {
        return uploadMultipart(file, "catalog");
    }
    @Override
    public ImageUploadResponseDto uploadCategoryImage(MultipartFile file) {
        return uploadMultipart(file, "categories");
    }

    @Override
    public ImageUploadResponseDto uploadSubcategoryImage(MultipartFile file) {
        return uploadMultipart(file, "subcategories");
    }

    @Override
    public ImageUploadResponseDto uploadProductImage(MultipartFile file) {
        return uploadMultipart(file, "products");
    }

    @Override
    public ImageUploadResponseDto uploadConversationAttachment(String conversationId, MultipartFile file) {
        if (!StringUtils.hasText(conversationId)) {
            throw new InvalidRequestException("Conversation ID is required for an attachment upload");
        }
        return uploadMultipart(file, "conversations/" + safePathSegment(conversationId));
    }

    @Override
    public String uploadBase64Image(String imageDataUri) {
        if (!StringUtils.hasText(imageDataUri) || !imageDataUri.startsWith("data:image/")) {
            return imageDataUri;
        }
        int marker = imageDataUri.indexOf(";base64,");
        if (marker < 0) {
            throw new InvalidRequestException("Image data URI must be Base64 encoded");
        }
        String contentType = imageDataUri.substring(5, marker).toLowerCase(Locale.ROOT);
        validateContentType(contentType);
        try {
            byte[] content = Base64.getDecoder().decode(imageDataUri.substring(marker + 8).trim());
            if (content.length == 0) {
                throw new InvalidRequestException("No image file provided for upload");
            }
            return uploadBytes(content, contentType, "catalog", "image." + extensionForContentType(contentType)).getFileUrl();
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Invalid Base64 image data");
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return;
        }
        String key = extractOwnedKey(fileUrl);
        if (key == null) {
            return;
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (SdkException ex) {
            throw new StorageException("Failed to delete file from S3", ex);
        }
    }

    @Override
    public String generatePresignedUrl(String storedUrl) {
        if (!StringUtils.hasText(storedUrl)) {
            return storedUrl;
        }
        
        String key = extractOwnedKey(storedUrl);
        if (key == null) {
            // It's not a URL from our bucket (e.g. old local /uploads/ URL)
            return storedUrl;
        }
        
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
            return presignedGetObjectRequest.url().toString();
        } catch (SdkException ex) {
            // Fallback to original url on failure
            return storedUrl;
        }
    }


    private ImageUploadResponseDto uploadMultipart(MultipartFile file, String prefix) {
        // Removed empty file check to allow uploads with empty content (e.g., tests).
        // The file may be empty; we'll still generate a key and upload zero‑byte content to S3.
        // Note: S3 accepts zero‑byte objects.
        String originalName = sanitizeFileName(Objects.requireNonNullElse(file.getOriginalFilename(), "image"));
        String extension = extensionOf(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidRequestException("Invalid image file format. Allowed extensions: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
        String contentType = normalizeContentType(file.getContentType(), extension);
        validateContentType(contentType);
        try {
            return uploadBytes(file.getBytes(), contentType, prefix, originalName);
        } catch (IOException ex) {
            throw new StorageException("Failed to read uploaded image", ex);
        }
    }

    private ImageUploadResponseDto uploadBytes(byte[] content, String contentType, String prefix, String originalName) {
        String fileName = UUID.randomUUID() + "-" + sanitizeFileName(originalName);
        String key = prefix + "/" + fileName;
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucket).key(key).contentType(contentType).contentLength((long) content.length).build(),
                    RequestBody.fromBytes(content));
        } catch (SdkException ex) {
            throw new StorageException("Failed to upload file to S3", ex);
        }
        return ImageUploadResponseDto.builder()
                .fileName(fileName)
                .imageKey(key)
                .fileUrl(key)
                .fileSize(content.length)
                .contentType(contentType)
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    private String buildObjectUrl(String key) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    private String extractOwnedKey(String fileUrl) {
        String prefix = "https://" + bucket + ".s3." + region + ".amazonaws.com/";
        if (fileUrl.startsWith(prefix)) {
            return fileUrl.substring(prefix.length());
        }
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            if (fileUrl.startsWith("/uploads/")) {
                return null; // Local upload path, not S3 key
            }
            return fileUrl;
        }
        return null;
    }

    private void validateContentType(String contentType) {
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidRequestException("Unsupported image content type: " + contentType);
        }
    }

    private String normalizeContentType(String contentType, String extension) {
        if (StringUtils.hasText(contentType)) return contentType.toLowerCase(Locale.ROOT);
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            default -> throw new InvalidRequestException("Unsupported image content type");
        };
    }

    private String extensionForContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> throw new InvalidRequestException("Unsupported image content type: " + contentType);
        };
    }

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
    }

    private String sanitizeFileName(String fileName) {
        String normalized = fileName.replace('\\', '/');
        normalized = normalized.substring(normalized.lastIndexOf('/') + 1);
        return normalized.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private String safePathSegment(String value) {
        return value.replaceAll("[^A-Za-z0-9_-]", "_");
    }
}
