package com.online.library.controllers.views;

import com.online.library.services.BookService;
import com.online.library.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final BookService bookService;

    @PostMapping("/books/{id}/upload-cover")
    public String uploadBookCover(
            @PathVariable Long id,
            @RequestParam("coverImage") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Wybierz plik do przesłania.");
            return "redirect:/books/" + id;
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            redirectAttributes.addFlashAttribute("error", "Plik musi być obrazem (JPG, PNG, GIF).");
            return "redirect:/books/" + id;
        }

        try {
            String filePath = fileStorageService.storeFile(file, "covers");

            // Update book with cover image path
            bookService.findById(id).ifPresent(book -> {
                book.setCoverImagePath(filePath);
                bookService.save(book);
            });

            log.info("Cover uploaded for book id={}: {}", id, filePath);
            redirectAttributes.addFlashAttribute("success", "Okładka została przesłana pomyślnie!");
        } catch (Exception e) {
            log.error("Failed to upload cover for book id={}", id, e);
            redirectAttributes.addFlashAttribute("error", "Błąd podczas przesyłania pliku: " + e.getMessage());
        }

        return "redirect:/books/" + id;
    }

    @GetMapping("/uploads/{directory}/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String directory,
            @PathVariable String filename) {

        String filePath = directory + "/" + filename;
        Resource resource = fileStorageService.loadFileAsResource(filePath);

        String contentType = "application/octet-stream";
        if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (filename.toLowerCase().endsWith(".png")) {
            contentType = "image/png";
        } else if (filename.toLowerCase().endsWith(".gif")) {
            contentType = "image/gif";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
