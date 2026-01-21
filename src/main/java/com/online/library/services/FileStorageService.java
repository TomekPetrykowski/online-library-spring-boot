package com.online.library.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file, String directory);

    Resource loadFileAsResource(String filePath);

    void deleteFile(String filePath);
}
