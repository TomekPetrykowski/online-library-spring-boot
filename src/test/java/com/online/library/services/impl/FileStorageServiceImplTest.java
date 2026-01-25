package com.online.library.services.impl;

import com.online.library.exceptions.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceImplTest {

    @TempDir
    Path tempDir;

    private FileStorageServiceImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new FileStorageServiceImpl(tempDir.toString());
    }

    @Test
    void testStoreFileSuccessfully() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes());
        String directory = "covers";

        // When
        String result = underTest.storeFile(file, directory);

        // Then
        assertThat(result).startsWith(directory + "/");
        assertThat(result).endsWith(".jpg");

        // Verify file exists
        Path storedFile = tempDir.resolve(result);
        assertThat(Files.exists(storedFile)).isTrue();
        assertThat(Files.readString(storedFile)).isEqualTo("test image content");
    }

    @Test
    void testStoreFileWithInvalidPathThrowsException() {
        // Given - attempt path traversal attack
        MultipartFile file = new MockMultipartFile(
                "file",
                "../../../etc/passwd",
                "text/plain",
                "malicious content".getBytes());
        String directory = "uploads";

        // When/Then
        assertThatThrownBy(() -> underTest.storeFile(file, directory))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Invalid file path");
    }

    @Test
    void testStoreFileGeneratesUniqueFilename() throws IOException {
        // Given
        MultipartFile file1 = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content1".getBytes());
        MultipartFile file2 = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content2".getBytes());
        String directory = "covers";

        // When
        String result1 = underTest.storeFile(file1, directory);
        String result2 = underTest.storeFile(file2, directory);

        // Then
        assertThat(result1).isNotEqualTo(result2);
        assertThat(Files.exists(tempDir.resolve(result1))).isTrue();
        assertThat(Files.exists(tempDir.resolve(result2))).isTrue();
    }

    @Test
    void testStoreFilePreservesExtension() throws IOException {
        // Given
        MultipartFile pngFile = new MockMultipartFile("file", "image.png", "image/png", "content".getBytes());
        MultipartFile gifFile = new MockMultipartFile("file", "image.gif", "image/gif", "content".getBytes());
        String directory = "images";

        // When
        String pngResult = underTest.storeFile(pngFile, directory);
        String gifResult = underTest.storeFile(gifFile, directory);

        // Then
        assertThat(pngResult).endsWith(".png");
        assertThat(gifResult).endsWith(".gif");
    }

    @Test
    void testLoadFileAsResourceSuccessfully() throws IOException {
        // Given - store a file first
        Path targetDir = tempDir.resolve("covers");
        Files.createDirectories(targetDir);
        Path testFile = targetDir.resolve("test-file.txt");
        Files.writeString(testFile, "file content");

        // When
        Resource resource = underTest.loadFileAsResource("covers/test-file.txt");

        // Then
        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
    }

    @Test
    void testLoadFileAsResourceNotFoundThrowsException() {
        // Given
        String nonExistentPath = "covers/non-existent-file.jpg";

        // When/Then
        assertThatThrownBy(() -> underTest.loadFileAsResource(nonExistentPath))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("File not found");
    }

    @Test
    void testDeleteFileSuccessfully() throws IOException {
        // Given - create a file first
        Path targetDir = tempDir.resolve("covers");
        Files.createDirectories(targetDir);
        Path testFile = targetDir.resolve("to-delete.txt");
        Files.writeString(testFile, "to be deleted");
        assertThat(Files.exists(testFile)).isTrue();

        // When
        underTest.deleteFile("covers/to-delete.txt");

        // Then
        assertThat(Files.exists(testFile)).isFalse();
    }

    @Test
    void testDeleteNonExistentFileDoesNotThrowException() {
        // Given
        String nonExistentPath = "covers/does-not-exist.jpg";

        // When/Then - should not throw
        underTest.deleteFile(nonExistentPath);
    }

    @Test
    void testStoreFileWithoutExtension() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file",
                "noextension",
                "application/octet-stream",
                "content".getBytes());
        String directory = "files";

        // When
        String result = underTest.storeFile(file, directory);

        // Then
        assertThat(result).startsWith(directory + "/");
        // Should not have extension since original didn't have one
        assertThat(Files.exists(tempDir.resolve(result))).isTrue();
    }

    @Test
    void testStoreFileCreatesSubdirectoryIfNotExists() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        String directory = "new/nested/directory";

        // When
        String result = underTest.storeFile(file, directory);

        // Then
        assertThat(result).startsWith(directory + "/");
        assertThat(Files.exists(tempDir.resolve(result))).isTrue();
    }
}
