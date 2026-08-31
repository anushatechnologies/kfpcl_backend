package com.kfpcl.service;

import com.kfpcl.serviceImpl.ImageUploadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageUploadServicePresignTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private PresignedGetObjectRequest presignedGetObjectRequest;

    private ImageUploadServiceImpl imageUploadService;

    private final String bucket = "kfpcl-backend-images-2026";
    private final String region = "ap-south-2";

    @BeforeEach
    void setUp() {
        imageUploadService = new ImageUploadServiceImpl(s3Client, s3Presigner, bucket, region);
    }

    @Test
    void testGeneratePresignedUrl_ValidS3Url() throws Exception {
        String fullS3Url = "https://kfpcl-backend-images-2026.s3.ap-south-2.amazonaws.com/catalog/abc.jpg";
        URL fakePresignedUrl = new URL("https://kfpcl-backend-images-2026.s3.ap-south-2.amazonaws.com/catalog/abc.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Signature=123");
        
        when(s3Presigner.presignGetObject(ArgumentMatchers.any(GetObjectPresignRequest.class)))
                .thenReturn(presignedGetObjectRequest);
        when(presignedGetObjectRequest.url()).thenReturn(fakePresignedUrl);

        String result = imageUploadService.generatePresignedUrl(fullS3Url);

        assertEquals(fakePresignedUrl.toString(), result);
    }

    @Test
    void testGeneratePresignedUrl_JustKey() throws Exception {
        String justKey = "catalog/abc.jpg";
        URL fakePresignedUrl = new URL("https://kfpcl-backend-images-2026.s3.ap-south-2.amazonaws.com/catalog/abc.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Signature=123");

        when(s3Presigner.presignGetObject(ArgumentMatchers.any(GetObjectPresignRequest.class)))
                .thenReturn(presignedGetObjectRequest);
        when(presignedGetObjectRequest.url()).thenReturn(fakePresignedUrl);

        String result = imageUploadService.generatePresignedUrl(justKey);

        assertEquals(fakePresignedUrl.toString(), result);
    }

    @Test
    void testGeneratePresignedUrl_LocalUrl() {
        String localUrl = "/uploads/abc.jpg";
        String result = imageUploadService.generatePresignedUrl(localUrl);

        // Should return original string without presigning
        assertEquals(localUrl, result);
    }

    @Test
    void testGeneratePresignedUrl_NullOrEmpty() {
        assertEquals(null, imageUploadService.generatePresignedUrl(null));
        assertEquals("", imageUploadService.generatePresignedUrl(""));
    }
}
