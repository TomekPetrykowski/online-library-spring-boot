package com.online.library.repositories;

import com.online.library.domain.entities.BookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long> {
    Optional<BookEntity> findByIsbn(String isbn);

    @Query("SELECT b FROM BookEntity b " +
            "LEFT JOIN b.authors a " +
            "LEFT JOIN b.genres g " +
            "WHERE (:searchTerm IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "OR (:searchTerm IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "OR (:searchTerm IS NULL OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "OR (:searchTerm IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<BookEntity> searchBooks(@Param("searchTerm") String searchTerm, Pageable pageable);

    Page<BookEntity> findAllByOrderByAverageRatingDesc(Pageable pageable);
}
