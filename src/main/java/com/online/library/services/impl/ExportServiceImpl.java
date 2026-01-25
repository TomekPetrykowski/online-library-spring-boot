package com.online.library.services.impl;

import com.online.library.domain.dto.AuthorStatDto;
import com.online.library.domain.dto.BookStatDto;
import com.online.library.domain.dto.UserStatDto;
import com.online.library.services.ExportService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ExportServiceImpl implements ExportService {

    private static final String CSV_SEPARATOR = ";";
    private static final String BOM = "\uFEFF"; // UTF-8 BOM for Excel compatibility

    @Override
    public byte[] exportPopularBooksToCsv(List<BookStatDto> books) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(baos, true, StandardCharsets.UTF_8)) {
            writer.print(BOM);
            // Header
            writer.println("ID" + CSV_SEPARATOR +
                    "Tytuł" + CSV_SEPARATOR +
                    "Średnia ocena" + CSV_SEPARATOR +
                    "Liczba rezerwacji");

            // Data
            for (BookStatDto book : books) {
                writer.println(
                        escapeCsv(String.valueOf(book.getId())) + CSV_SEPARATOR +
                                escapeCsv(book.getTitle()) + CSV_SEPARATOR +
                                escapeCsv(book.getAverageRating() != null ? book.getAverageRating().toString() : "N/A")
                                + CSV_SEPARATOR +
                                escapeCsv(String.valueOf(book.getReservationCount())));
            }
        }
        return baos.toByteArray();
    }

    @Override
    public byte[] exportReadAuthorsToCsv(List<AuthorStatDto> authors) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(baos, true, StandardCharsets.UTF_8)) {
            writer.print(BOM);
            // Header
            writer.println("ID" + CSV_SEPARATOR +
                    "Imię" + CSV_SEPARATOR +
                    "Nazwisko" + CSV_SEPARATOR +
                    "Liczba wypożyczeń");

            // Data
            for (AuthorStatDto author : authors) {
                writer.println(
                        escapeCsv(String.valueOf(author.getId())) + CSV_SEPARATOR +
                                escapeCsv(author.getName()) + CSV_SEPARATOR +
                                escapeCsv(author.getLastName()) + CSV_SEPARATOR +
                                escapeCsv(String.valueOf(author.getLoanCount())));
            }
        }
        return baos.toByteArray();
    }

    @Override
    public byte[] exportActiveUsersToCsv(List<UserStatDto> users) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(baos, true, StandardCharsets.UTF_8)) {
            writer.print(BOM);
            // Header
            writer.println("ID" + CSV_SEPARATOR +
                    "Nazwa użytkownika" + CSV_SEPARATOR +
                    "Email" + CSV_SEPARATOR +
                    "Liczba rezerwacji");

            // Data
            for (UserStatDto user : users) {
                writer.println(
                        escapeCsv(String.valueOf(user.getId())) + CSV_SEPARATOR +
                                escapeCsv(user.getUsername()) + CSV_SEPARATOR +
                                escapeCsv(user.getEmail()) + CSV_SEPARATOR +
                                escapeCsv(String.valueOf(user.getReservationCount())));
            }
        }
        return baos.toByteArray();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(CSV_SEPARATOR) || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
}
