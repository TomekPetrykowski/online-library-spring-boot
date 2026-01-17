package com.online.library.repositories;

import com.online.library.domain.entities.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends
        ListCrudRepository<BookEntity, Long>,
        PagingAndSortingRepository<BookEntity, Long> {
    Optional<BookEntity> findByIsbn(String isbn);
}
