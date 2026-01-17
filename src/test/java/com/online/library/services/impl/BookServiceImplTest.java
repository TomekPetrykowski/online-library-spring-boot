package com.online.library.services.impl;

import com.online.library.domain.dto.BookDto;
import com.online.library.domain.entities.BookEntity;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private Mapper<BookEntity, BookDto> bookMapper;

    @InjectMocks
    private BookServiceImpl underTest;

    @Test
    public void testThatBookIsSavedSuccessfully() {
        BookDto bookDto = BookDto.builder().id(1L).title("Test Book").build();
        BookEntity bookEntity = BookEntity.builder().id(1L).title("Test Book").build();

        when(bookMapper.mapFrom(bookDto)).thenReturn(bookEntity);
        when(bookRepository.save(any(BookEntity.class))).thenReturn(bookEntity);
        when(bookMapper.mapTo(bookEntity)).thenReturn(bookDto);

        BookDto result = underTest.save(bookDto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Book");
    }

    @Test
    public void testThatFindByIdReturnsBook() {
        BookEntity bookEntity = BookEntity.builder().id(1L).title("Test Book").build();
        BookDto bookDto = BookDto.builder().id(1L).title("Test Book").build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(bookEntity));
        when(bookMapper.mapTo(bookEntity)).thenReturn(bookDto);

        Optional<BookDto> result = underTest.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Test Book");
    }
}
