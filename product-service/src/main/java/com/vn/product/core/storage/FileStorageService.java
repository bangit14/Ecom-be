package com.vn.product.core.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadImage(MultipartFile file, String folder);
    void deleteFile(String fileUrl);
}
