package com.online.library.utils;

import com.online.library.domain.entities.*;
import com.online.library.domain.enums.ReservationStatus;
import com.online.library.domain.enums.UserRole;

public class TestDataUtil {
    private TestDataUtil(){}

    public static AuthorEntity createTestAuthor() {
        return AuthorEntity.builder()
                .name("H.P.")
                .lastName("Lovecraft")
                .bio("American writer of weird and horror fiction.")
                .build();
    }

    public static GenreEntity createTestGenre() {
        return GenreEntity.builder()
                .name("Horror")
                .build();
    }

    public static UserEntity createTestUser() {
        return UserEntity.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .role(UserRole.USER)
                .enabled(true)
                .build();
    }

    public static BookEntity createTestBook() {
        return BookEntity.builder()
                .title("The Shadow over Innsmouth")
                .isbn("978-0-141-18706-8")
                .copiesAvailable(5)
                .build();
    }

    public static ReservationEntity createTestReservation(UserEntity user, BookEntity book) {
        return ReservationEntity.builder()
                .user(user)
                .book(book)
                .status(ReservationStatus.OCZEKUJĄCA)
                .build();
    }

    public static RatingEntity createTestRating(UserEntity user, BookEntity book) {
        return RatingEntity.builder()
                .user(user)
                .book(book)
                .rating(5)
                .build();
    }

    public static CommentEntity createTestComment(UserEntity user, BookEntity book) {
        return CommentEntity.builder()
                .user(user)
                .book(book)
                .content("Great book!")
                .build();
    }
}
