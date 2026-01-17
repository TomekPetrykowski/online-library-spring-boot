package com.online.library.services.impl;

import com.online.library.domain.dto.BookDto;
import com.online.library.domain.entities.BookEntity;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.BookRepository;
import com.online.library.services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final Mapper<BookEntity, BookDto> bookMapper;

    @Override
    public BookDto save(BookDto bookDto) {
        BookEntity bookEntity = bookMapper.mapFrom(bookDto);
        BookEntity savedBookEntity = bookRepository.save(bookEntity);
        return bookMapper.mapTo(savedBookEntity);
    }

    @Override
    public List<BookDto> findAll() {
        return StreamSupport.stream(bookRepository.findAll().spliterator(), false)
                .map(bookMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BookDto> findById(Long id) {
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
    public void delete(Long id) {
        bookRepository.deleteById(id);
    }
}
