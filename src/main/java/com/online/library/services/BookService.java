package com.online.library.services;

import com.online.library.domain.dto.BookDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService extends BaseService<BookDto, Long> {
    Page<BookDto> searchBooks(String searchTerm, Pageable pageable);
    Page<BookDto> getPopularBooks(Pageable pageable);
}
