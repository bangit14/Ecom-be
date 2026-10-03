package com.vn.product.core.storage;

import com.vn.product.core.config.MinioProperties;
import com.vn.product.core.exception.AppException;
import com.vn.product.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MinioStorageService")
public class MinioStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final MinioProperties minioProperties;

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID() + "_" + originalFilename;
        String key = folder + "/" + fileName;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(minioProperties.getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String imageUrl = minioProperties.getBaseUrl() + "/" + key;
            log.info("Uploaded to MinIO successfully: {}", imageUrl);
            return imageUrl;

        } catch (Exception e) {
            log.error("Failed to upload to MinIO", e);
            throw new AppException(ErrorCode.CANNOT_UPLOAD_IMAGE);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;

        try {
            String key = fileUrl.substring(fileUrl.indexOf(minioProperties.getBucket())
                    + minioProperties.getBucket().length() + 1);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(minioProperties.getBucket())
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Deleted from MinIO: {}", key);

        } catch (Exception e) {
            log.warn("Failed to delete file from MinIO: {}", fileUrl, e);
        }
    }
}
