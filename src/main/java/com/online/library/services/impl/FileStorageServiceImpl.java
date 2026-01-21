package com.online.library.services.impl;

import com.online.library.exceptions.FileStorageException;
import com.online.library.services.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path uploadDir;

    public FileStorageServiceImpl(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
            log.info("Upload directory initialized at: {}", this.uploadDir);
        } catch (IOException e) {
            throw new FileStorageException("Could not create upload directory", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String directory) {
        log.debug("Storing file in directory: {}", directory);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        if (originalFilename.contains("..")) {
            throw new FileStorageException("Invalid file path: " + originalFilename);
        }

        String fileExtension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFilename.substring(dotIndex);
        }

        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        try {
            Path targetDir = this.uploadDir.resolve(directory);
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = directory + "/" + uniqueFilename;
            log.info("File stored successfully: {}", relativePath);
            return relativePath;
        } catch (IOException e) {
            log.error("Failed to store file: {}", originalFilename, e);
            throw new FileStorageException("Failed to store file: " + originalFilename, e);
        }
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        log.debug("Loading file: {}", filePath);
        try {
            Path path = this.uploadDir.resolve(filePath).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                log.error("File not found or not readable: {}", filePath);
                throw new FileStorageException("File not found: " + filePath);
            }
        } catch (MalformedURLException e) {
            log.error("File not found: {}", filePath, e);
            throw new FileStorageException("File not found: " + filePath, e);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        log.debug("Deleting file: {}", filePath);
        try {
            Path path = this.uploadDir.resolve(filePath).normalize();
            Files.deleteIfExists(path);
            log.info("File deleted: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
        }
    }
}
