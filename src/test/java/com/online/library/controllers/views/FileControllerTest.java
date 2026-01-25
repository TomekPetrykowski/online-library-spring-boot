package com.online.library.controllers.views;

import com.online.library.services.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    void testDownloadJpgFile() throws Exception {
        byte[] fileContent = "fake jpg content".getBytes();
        Resource resource = new ByteArrayResource(fileContent);

        when(fileStorageService.loadFileAsResource("covers/test.jpg")).thenReturn(resource);

        mockMvc.perform(get("/uploads/covers/test.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"test.jpg\""));

        verify(fileStorageService).loadFileAsResource("covers/test.jpg");
    }

    @Test
    void testDownloadJpegFile() throws Exception {
        byte[] fileContent = "fake jpeg content".getBytes();
        Resource resource = new ByteArrayResource(fileContent);

        when(fileStorageService.loadFileAsResource("covers/photo.jpeg")).thenReturn(resource);

        mockMvc.perform(get("/uploads/covers/photo.jpeg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"photo.jpeg\""));

        verify(fileStorageService).loadFileAsResource("covers/photo.jpeg");
    }

    @Test
    void testDownloadPngFile() throws Exception {
        byte[] fileContent = "fake png content".getBytes();
        Resource resource = new ByteArrayResource(fileContent);

        when(fileStorageService.loadFileAsResource("covers/image.png")).thenReturn(resource);

        mockMvc.perform(get("/uploads/covers/image.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"image.png\""));

        verify(fileStorageService).loadFileAsResource("covers/image.png");
    }

    @Test
    void testDownloadGifFile() throws Exception {
        byte[] fileContent = "fake gif content".getBytes();
        Resource resource = new ByteArrayResource(fileContent);

        when(fileStorageService.loadFileAsResource("images/animated.gif")).thenReturn(resource);

        mockMvc.perform(get("/uploads/images/animated.gif"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/gif"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"animated.gif\""));

        verify(fileStorageService).loadFileAsResource("images/animated.gif");
    }

    @Test
    void testDownloadOtherFileType() throws Exception {
        byte[] fileContent = "fake pdf content".getBytes();
        Resource resource = new ByteArrayResource(fileContent);

        when(fileStorageService.loadFileAsResource("documents/file.pdf")).thenReturn(resource);

        mockMvc.perform(get("/uploads/documents/file.pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/octet-stream"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"file.pdf\""));

        verify(fileStorageService).loadFileAsResource("documents/file.pdf");
    }

    @Test
    void testDownloadFileWithUppercaseExtension() throws Exception {
        byte[] fileContent = "fake jpg content".getBytes();
        Resource resource = new ByteArrayResource(fileContent);

        when(fileStorageService.loadFileAsResource("covers/test.JPG")).thenReturn(resource);

        mockMvc.perform(get("/uploads/covers/test.JPG"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"test.JPG\""));

        verify(fileStorageService).loadFileAsResource("covers/test.JPG");
    }

    @Test
    void testDownloadFileWithMixedCaseExtension() throws Exception {
        byte[] fileContent = "fake png content".getBytes();
        Resource resource = new ByteArrayResource(fileContent);

        when(fileStorageService.loadFileAsResource("covers/test.PNG")).thenReturn(resource);

        mockMvc.perform(get("/uploads/covers/test.PNG"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"test.PNG\""));

        verify(fileStorageService).loadFileAsResource("covers/test.PNG");
    }

    @Test
    void testDownloadFileWithNoExtension() throws Exception {
        byte[] fileContent = "some content".getBytes();
        Resource resource = new ByteArrayResource(fileContent);

        when(fileStorageService.loadFileAsResource("misc/readme")).thenReturn(resource);

        mockMvc.perform(get("/uploads/misc/readme"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/octet-stream"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"readme\""));

        verify(fileStorageService).loadFileAsResource("misc/readme");
    }

    @Test
    void testDownloadJpegWithCapitalJPEG() throws Exception {
        byte[] fileContent = "fake jpeg content".getBytes();
        Resource resource = new ByteArrayResource(fileContent);

        when(fileStorageService.loadFileAsResource("covers/photo.JPEG")).thenReturn(resource);

        mockMvc.perform(get("/uploads/covers/photo.JPEG"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"photo.JPEG\""));

        verify(fileStorageService).loadFileAsResource("covers/photo.JPEG");
    }

    @Test
    void testDownloadGifWithCapitalGIF() throws Exception {
        byte[] fileContent = "fake gif content".getBytes();
        Resource resource = new ByteArrayResource(fileContent);

        when(fileStorageService.loadFileAsResource("images/animated.GIF")).thenReturn(resource);

        mockMvc.perform(get("/uploads/images/animated.GIF"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/gif"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"animated.GIF\""));

        verify(fileStorageService).loadFileAsResource("images/animated.GIF");
    }
}
