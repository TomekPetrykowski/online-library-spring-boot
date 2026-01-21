package com.online.library.services.impl;

import com.online.library.domain.dto.BookDto;
import com.online.library.domain.entities.BookEntity;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.BookRepository;
import com.online.library.services.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final Mapper<BookEntity, BookDto> bookMapper;

    @Override
    public BookDto save(BookDto bookDto) {
        log.info("Zapisywanie książki: {}", bookDto.getTitle());
        BookEntity bookEntity = bookMapper.mapFrom(bookDto);
        BookEntity savedBookEntity = bookRepository.save(bookEntity);
        log.debug("Książka zapisana z id: {}", savedBookEntity.getId());
        return bookMapper.mapTo(savedBookEntity);
    }

    @Override
    public Page<BookDto> findAll(Pageable pageable) {
        Page<BookEntity> foundBooks = bookRepository.findAll(pageable);
        return foundBooks.map(bookMapper::mapTo);
    }

    @Override
    public Optional<BookDto> findById(Long id) {
        log.debug("Znajdowanie książki o id: {}", id);
        return bookRepository.findById(id).map(bookMapper::mapTo);
    }

    @Override
    public boolean isExists(Long id) {
        return bookRepository.existsById(id);
    }

    @Override
    public BookDto partialUpdate(Long id, BookDto bookDto) {
        bookDto.setId(id);

        return bookRepository.findById(id).map(existingBook -> {
            Optional.ofNullable(bookDto.getTitle()).ifPresent(existingBook::setTitle);
            Optional.ofNullable(bookDto.getDescription()).ifPresent(existingBook::setDescription);
            Optional.ofNullable(bookDto.getPublisher()).ifPresent(existingBook::setPublisher);
            Optional.ofNullable(bookDto.getPublishYear()).ifPresent(existingBook::setPublishYear);
            Optional.ofNullable(bookDto.getIsbn()).ifPresent(existingBook::setIsbn);
            Optional.ofNullable(bookDto.getCoverImagePath()).ifPresent(existingBook::setCoverImagePath);
            Optional.ofNullable(bookDto.getCopiesAvailable()).ifPresent(existingBook::setCopiesAvailable);
            Optional.ofNullable(bookDto.getAverageRating()).ifPresent(existingBook::setAverageRating);
            return bookMapper.mapTo(bookRepository.save(existingBook));
        }).orElseThrow(() -> new ResourceNotFoundException("Book does not exist"));
    }

    @Override
    public Page<BookDto> searchBooks(String searchTerm, Pageable pageable) {
        Page<BookEntity> books = bookRepository.searchBooks(searchTerm, pageable);
        return books.map(bookMapper::mapTo);
    }

    @Override
    public Page<BookDto> getPopularBooks(Pageable pageable) {
        Page<BookEntity> books = bookRepository.findAllByOrderByAverageRatingDesc(pageable);
        return books.map(bookMapper::mapTo);
    }

    @Override
    public void delete(Long id) {
        log.info("Usuwanie książki o id: {}", id);
        bookRepository.deleteById(id);
    }
}
