package com.kfpcl.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Component
public class ImageUtils {

    @Value("${file.upload.dir:uploads/catalog/}")
    private String uploadDir;

    /**
     * Checks if the imageUrl is a Base64 encoded data URI. If so, decodes and
     * saves it to the disk, returning the relative file URL. Otherwise, returns
     * the original string.
     *
     * @param imageUrl the image URL or Base64 data URI
     * @return the processed file URL or the original URL
     */
    public String processBase64Image(String imageUrl) {
        if (!StringUtils.hasText(imageUrl) || !imageUrl.startsWith("data:image/")) {
            return imageUrl;
        }

        int base64Index = imageUrl.indexOf(";base64,");
        if (base64Index == -1) {
            return imageUrl;
        }

        try {
            String mimeType = imageUrl.substring(11, base64Index);
            String extension = mimeType.toLowerCase();
            if (extension.equals("jpeg")) {
                extension = "jpg";
            }
            if (extension.contains(";")) {
                extension = extension.split(";")[0];
            }

            String base64Data = imageUrl.substring(base64Index + 8);
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data.trim());

            Path targetDir = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String uniqueFileName = "cat_img_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;
            Path targetPath = targetDir.resolve(uniqueFileName);

            Files.write(targetPath, decodedBytes);

            return "/api/v1/uploads/" + uniqueFileName;
        } catch (Exception e) {
            System.err.println("Failed to process base64 image: " + e.getMessage());
            return imageUrl;
        }
    }
}
