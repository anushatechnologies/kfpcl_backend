package com.kfpcl.service;

import com.kfpcl.dto.ImageUploadResponseDto;
import com.kfpcl.exception.StorageException;
import com.kfpcl.serviceImpl.ImageUploadServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceImplTest {

    @Mock
    private S3Client s3Client;

    private ImageUploadServiceImpl service() {
        return new ImageUploadServiceImpl(s3Client, "kfpcl-backend-images-2026", "ap-south-1");
    }

    @Test
    void uploadsImageToS3AndReturnsUsableUrl() {
        ImageUploadResponseDto result = service().uploadCatalogImage(new MockMultipartFile(
                "file", "fresh milk.jpg", "image/jpeg", "image-content".getBytes()));

        ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(request.capture(), any(RequestBody.class));
        assertEquals("kfpcl-backend-images-2026", request.getValue().bucket());
        assertTrue(request.getValue().key().matches("catalog/[0-9a-f-]+-fresh_milk\\.jpg"));
        assertEquals("image/jpeg", request.getValue().contentType());
        assertEquals(request.getValue().key(), result.getFileUrl());
        assertEquals(request.getValue().key(), result.getImageKey());
    }

    @Test
    void reportsS3UploadFailure() {
        doThrow(S3Exception.builder().message("Access denied").build())
                .when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        assertThrows(StorageException.class, () -> service().uploadCatalogImage(new MockMultipartFile(
                "file", "milk.jpg", "image/jpeg", new byte[]{1})));
    }

    @Test
    void createsConversationScopedObjectKey() {
        service().uploadConversationAttachment("conv_123", new MockMultipartFile(
                "file", "note.png", "image/png", new byte[]{1}));

        ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(request.capture(), any(RequestBody.class));
        assertTrue(request.getValue().key().matches("conversations/conv_123/[0-9a-f-]+-note\\.png"));
    }

    @Test
    void deletesOnlyOwnedS3UrlsAndReportsDeleteFailure() {
        String url = "https://kfpcl-backend-images-2026.s3.ap-south-1.amazonaws.com/catalog/a.jpg";
        service().deleteFile(url);
        ArgumentCaptor<DeleteObjectRequest> request = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(request.capture());
        assertEquals("catalog/a.jpg", request.getValue().key());

        // Test deleting raw relative key
        String relativeKey = "catalog/b.jpg";
        service().deleteFile(relativeKey);
        verify(s3Client, times(2)).deleteObject(request.capture());
        assertEquals("catalog/b.jpg", request.getValue().key());

        doThrow(S3Exception.builder().message("Access denied").build()).when(s3Client).deleteObject(any(DeleteObjectRequest.class));
        assertThrows(StorageException.class, () -> service().deleteFile(url));
        service().deleteFile("https://example.test/other-image.jpg");
        verify(s3Client, times(3)).deleteObject(any(DeleteObjectRequest.class));
    }
}
