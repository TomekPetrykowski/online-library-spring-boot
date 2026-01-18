package com.online.library.repositories;

import com.online.library.domain.entities.BookEntity;
import com.online.library.domain.entities.RatingEntity;
import com.online.library.domain.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<RatingEntity, Long> {

    List<RatingEntity> findByBookId(Long bookId);

    List<RatingEntity> findByUserId(Long userId);

    Optional<RatingEntity> findByUserAndBook(UserEntity user, BookEntity book);

    @Query("SELECT r FROM RatingEntity r WHERE r.user = :user AND r.book = :book AND r.createdAt >= :since")
    Optional<RatingEntity> findRecentRatingByUserAndBook(
            @Param("user") UserEntity user,
            @Param("book") BookEntity book,
            @Param("since") LocalDateTime since);

    @Query("SELECT AVG(r.rating) FROM RatingEntity r WHERE r.book.id = :bookId")
    Optional<BigDecimal> calculateAverageRatingByBookId(@Param("bookId") Long bookId);

    @Query("SELECT COUNT(r) FROM RatingEntity r WHERE r.book.id = :bookId")
    Long countRatingsByBookId(@Param("bookId") Long bookId);
}
