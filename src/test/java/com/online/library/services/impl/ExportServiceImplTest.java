package com.online.library.services.impl;

import com.online.library.domain.dto.AuthorStatDto;
import com.online.library.domain.dto.BookStatDto;
import com.online.library.domain.dto.UserStatDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExportServiceImplTest {

    private ExportServiceImpl underTest;

    private static final String BOM = "\uFEFF";
    private static final String CSV_SEPARATOR = ";";

    @BeforeEach
    void setUp() {
        underTest = new ExportServiceImpl();
    }

    // ==================== exportPopularBooksToCsv tests ====================

    @Test
    void testExportPopularBooksToCsvWithData() {
        // Given
        List<BookStatDto> books = List.of(
                BookStatDto.builder()
                        .id(1L)
                        .title("Test Book 1")
                        .averageRating(BigDecimal.valueOf(4.5))
                        .reservationCount(10L)
                        .build(),
                BookStatDto.builder()
                        .id(2L)
                        .title("Test Book 2")
                        .averageRating(BigDecimal.valueOf(3.8))
                        .reservationCount(5L)
                        .build());

        // When
        byte[] result = underTest.exportPopularBooksToCsv(books);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Then
        assertThat(csvContent).startsWith(BOM);
        assertThat(csvContent).contains(
                "ID" + CSV_SEPARATOR + "Tytuł" + CSV_SEPARATOR + "Średnia ocena" + CSV_SEPARATOR + "Liczba rezerwacji");
        assertThat(csvContent)
                .contains("1" + CSV_SEPARATOR + "Test Book 1" + CSV_SEPARATOR + "4.5" + CSV_SEPARATOR + "10");
        assertThat(csvContent)
                .contains("2" + CSV_SEPARATOR + "Test Book 2" + CSV_SEPARATOR + "3.8" + CSV_SEPARATOR + "5");
    }

    @Test
    void testExportPopularBooksToCsvWithEmptyList() {
        // Given
        List<BookStatDto> books = Collections.emptyList();

        // When
        byte[] result = underTest.exportPopularBooksToCsv(books);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Then
        assertThat(csvContent).startsWith(BOM);
        assertThat(csvContent).contains(
                "ID" + CSV_SEPARATOR + "Tytuł" + CSV_SEPARATOR + "Średnia ocena" + CSV_SEPARATOR + "Liczba rezerwacji");
        // Should only contain header, no data rows
        String[] lines = csvContent.split("\n");
        assertThat(lines).hasSize(1);
    }

    @Test
    void testExportPopularBooksToCsvWithNullAverageRating() {
        // Given
        List<BookStatDto> books = List.of(
                BookStatDto.builder()
                        .id(1L)
                        .title("Unrated Book")
                        .averageRating(null)
                        .reservationCount(3L)
                        .build());

        // When
        byte[] result = underTest.exportPopularBooksToCsv(books);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Then
        assertThat(csvContent)
                .contains("1" + CSV_SEPARATOR + "Unrated Book" + CSV_SEPARATOR + "N/A" + CSV_SEPARATOR + "3");
    }

    @Test
    void testExportPopularBooksToCsvWithSpecialCharactersInTitle() {
        // Given
        List<BookStatDto> books = List.of(
                BookStatDto.builder()
                        .id(1L)
                        .title("Book; with \"special\" chars\nand newlines")
                        .averageRating(BigDecimal.valueOf(4.0))
                        .reservationCount(7L)
                        .build());

        // When
        byte[] result = underTest.exportPopularBooksToCsv(books);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Then - special characters should be properly escaped with quotes and
        // double-quotes
        assertThat(csvContent).contains("\"Book; with \"\"special\"\" chars");
    }

    // ==================== exportReadAuthorsToCsv tests ====================

    @Test
    void testExportReadAuthorsToCsvWithData() {
        // Given
        List<AuthorStatDto> authors = List.of(
                AuthorStatDto.builder()
                        .id(1L)
                        .name("John")
                        .lastName("Doe")
                        .loanCount(15L)
                        .build(),
                AuthorStatDto.builder()
                        .id(2L)
                        .name("Jane")
                        .lastName("Smith")
                        .loanCount(10L)
                        .build());

        // When
        byte[] result = underTest.exportReadAuthorsToCsv(authors);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Then
        assertThat(csvContent).startsWith(BOM);
        assertThat(csvContent).contains(
                "ID" + CSV_SEPARATOR + "Imię" + CSV_SEPARATOR + "Nazwisko" + CSV_SEPARATOR + "Liczba wypożyczeń");
        assertThat(csvContent).contains("1" + CSV_SEPARATOR + "John" + CSV_SEPARATOR + "Doe" + CSV_SEPARATOR + "15");
        assertThat(csvContent).contains("2" + CSV_SEPARATOR + "Jane" + CSV_SEPARATOR + "Smith" + CSV_SEPARATOR + "10");
    }

    @Test
    void testExportReadAuthorsToCsvWithEmptyList() {
        // Given
        List<AuthorStatDto> authors = Collections.emptyList();

        // When
        byte[] result = underTest.exportReadAuthorsToCsv(authors);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Then
        assertThat(csvContent).startsWith(BOM);
        assertThat(csvContent).contains(
                "ID" + CSV_SEPARATOR + "Imię" + CSV_SEPARATOR + "Nazwisko" + CSV_SEPARATOR + "Liczba wypożyczeń");
        String[] lines = csvContent.split("\n");
        assertThat(lines).hasSize(1);
    }

    @Test
    void testExportReadAuthorsToCsvWithSpecialCharactersInName() {
        // Given
        List<AuthorStatDto> authors = List.of(
                AuthorStatDto.builder()
                        .id(1L)
                        .name("Jean-Pierre")
                        .lastName("O'Brien; Jr.")
                        .loanCount(8L)
                        .build());

        // When
        byte[] result = underTest.exportReadAuthorsToCsv(authors);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Then
        assertThat(csvContent).contains("Jean-Pierre");
        assertThat(csvContent).contains("\"O'Brien; Jr.\""); // semicolon requires quotes
    }

    // ==================== exportActiveUsersToCsv tests ====================

    @Test
    void testExportActiveUsersToCsvWithData() {
        // Given
        List<UserStatDto> users = List.of(
                UserStatDto.builder()
                        .id(1L)
                        .username("activeuser1")
                        .email("active1@example.com")
                        .reservationCount(20L)
                        .build(),
                UserStatDto.builder()
                        .id(2L)
                        .username("activeuser2")
                        .email("active2@example.com")
                        .reservationCount(15L)
                        .build());

        // When
        byte[] result = underTest.exportActiveUsersToCsv(users);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Then
        assertThat(csvContent).startsWith(BOM);
        assertThat(csvContent).contains("ID" + CSV_SEPARATOR + "Nazwa użytkownika" + CSV_SEPARATOR + "Email"
                + CSV_SEPARATOR + "Liczba rezerwacji");
        assertThat(csvContent).contains(
                "1" + CSV_SEPARATOR + "activeuser1" + CSV_SEPARATOR + "active1@example.com" + CSV_SEPARATOR + "20");
        assertThat(csvContent).contains(
                "2" + CSV_SEPARATOR + "activeuser2" + CSV_SEPARATOR + "active2@example.com" + CSV_SEPARATOR + "15");
    }

    @Test
    void testExportActiveUsersToCsvWithEmptyList() {
        // Given
        List<UserStatDto> users = Collections.emptyList();

        // When
        byte[] result = underTest.exportActiveUsersToCsv(users);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Then
        assertThat(csvContent).startsWith(BOM);
        assertThat(csvContent).contains("ID" + CSV_SEPARATOR + "Nazwa użytkownika" + CSV_SEPARATOR + "Email"
                + CSV_SEPARATOR + "Liczba rezerwacji");
        String[] lines = csvContent.split("\n");
        assertThat(lines).hasSize(1);
    }

    @Test
    void testExportActiveUsersToCsvWithSpecialCharactersInUsername() {
        // Given
        List<UserStatDto> users = List.of(
                UserStatDto.builder()
                        .id(1L)
                        .username("user;with\"special")
                        .email("special@example.com")
                        .reservationCount(5L)
                        .build());

        // When
        byte[] result = underTest.exportActiveUsersToCsv(users);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Then
        assertThat(csvContent).contains("\"user;with\"\"special\""); // properly escaped
    }

    // ==================== escapeCsv edge cases tests ====================

    @Test
    void testExportWithNullValues() {
        // Given - AuthorStatDto with null name
        List<AuthorStatDto> authors = List.of(
                AuthorStatDto.builder()
                        .id(1L)
                        .name(null)
                        .lastName("Doe")
                        .loanCount(5L)
                        .build());

        // When
        byte[] result = underTest.exportReadAuthorsToCsv(authors);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Then - null should be converted to empty string
        assertThat(csvContent).contains("1" + CSV_SEPARATOR + "" + CSV_SEPARATOR + "Doe" + CSV_SEPARATOR + "5");
    }

    @Test
    void testExportWithCarriageReturn() {
        // Given
        List<BookStatDto> books = List.of(
                BookStatDto.builder()
                        .id(1L)
                        .title("Book with\rcarriage return")
                        .averageRating(BigDecimal.valueOf(4.0))
                        .reservationCount(3L)
                        .build());

        // When
        byte[] result = underTest.exportPopularBooksToCsv(books);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Then - carriage return should trigger quoting
        assertThat(csvContent).contains("\"Book with");
    }

    @Test
    void testExportWithOnlyQuotes() {
        // Given
        List<BookStatDto> books = List.of(
                BookStatDto.builder()
                        .id(1L)
                        .title("\"Quoted Title\"")
                        .averageRating(BigDecimal.valueOf(4.0))
                        .reservationCount(3L)
                        .build());

        // When
        byte[] result = underTest.exportPopularBooksToCsv(books);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Then - quotes should be doubled and entire value quoted
        assertThat(csvContent).contains("\"\"\"Quoted Title\"\"\"");
    }

    @Test
    void testExportReturnsValidUTF8() {
        // Given - Polish characters
        List<BookStatDto> books = List.of(
                BookStatDto.builder()
                        .id(1L)
                        .title("Książka z polskimi znakami: ąęćżźńłóś")
                        .averageRating(BigDecimal.valueOf(5.0))
                        .reservationCount(100L)
                        .build());

        // When
        byte[] result = underTest.exportPopularBooksToCsv(books);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Then
        assertThat(csvContent).contains("ąęćżźńłóś");
    }
}
