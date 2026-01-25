package com.online.library.services.impl;

import com.online.library.domain.dto.BookDto;
import com.online.library.domain.entities.BookEntity;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.BookRepository;
import com.online.library.utils.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        BookEntity bookEntity = TestDataUtil.createTestBook();
        BookDto bookDto = BookDto.builder()
                .title(bookEntity.getTitle())
                .isbn(bookEntity.getIsbn())
                .copiesAvailable(bookEntity.getCopiesAvailable())
                .build();

        when(bookMapper.mapFrom(bookDto)).thenReturn(bookEntity);
        when(bookRepository.save(any(BookEntity.class))).thenReturn(bookEntity);
        when(bookMapper.mapTo(bookEntity)).thenReturn(bookDto);

        BookDto result = underTest.save(bookDto);

        assertThat(result).isEqualTo(bookDto);
        verify(bookRepository, times(1)).save(bookEntity);
    }

    @Test
    public void testThatFindAllWithPageableReturnsPageOfBooks() {
        BookEntity bookEntity = TestDataUtil.createTestBook();
        BookDto bookDto = BookDto.builder().id(1L).title("The Shadow over Innsmouth").build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<BookEntity> bookPage = new PageImpl<>(List.of(bookEntity));

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(bookMapper.mapTo(bookEntity)).thenReturn(bookDto);

        Page<BookDto> result = underTest.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(bookDto);
    }

    @Test
    public void testThatFindByIdReturnsBookWhenExists() {
        BookEntity bookEntity = TestDataUtil.createTestBook();
        bookEntity.setId(1L);
        BookDto bookDto = BookDto.builder().id(1L).title("The Shadow over Innsmouth").build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(bookEntity));
        when(bookMapper.mapTo(bookEntity)).thenReturn(bookDto);

        Optional<BookDto> result = underTest.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("The Shadow over Innsmouth");
    }

    @Test
    public void testThatFindByIdReturnsEmptyWhenNotExists() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<BookDto> result = underTest.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    public void testThatIsExistsReturnsTrueWhenExists() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        boolean result = underTest.isExists(1L);

        assertThat(result).isTrue();
    }

    @Test
    public void testThatPartialUpdateUpdatesBookSuccessfully() {
        Long bookId = 1L;
        BookEntity existingBook = TestDataUtil.createTestBook();
        existingBook.setId(bookId);

        BookDto updateDto = BookDto.builder().title("New Title").build();
        BookDto updatedDto = BookDto.builder().id(bookId).title("New Title").build();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(BookEntity.class))).thenReturn(existingBook);
        when(bookMapper.mapTo(any(BookEntity.class))).thenReturn(updatedDto);

        BookDto result = underTest.partialUpdate(bookId, updateDto);

        assertThat(result.getTitle()).isEqualTo("New Title");
        verify(bookRepository, times(1)).save(existingBook);
    }

    @Test
    public void testThatPartialUpdateThrowsExceptionWhenBookDoesNotExist() {
        Long bookId = 1L;
        BookDto updateDto = BookDto.builder().title("New Title").build();

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.partialUpdate(bookId, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Book does not exist");
    }

    @Test
    public void testThatDeleteCallsRepository() {
        Long bookId = 1L;
        doNothing().when(bookRepository).deleteById(bookId);

        underTest.delete(bookId);

        verify(bookRepository, times(1)).deleteById(bookId);
    }

    @Test
    public void testThatSearchBooksReturnsPageOfBooks() {
        String searchTerm = "Java";
        BookEntity bookEntity = TestDataUtil.createTestBook();
        BookDto bookDto = BookDto.builder().id(1L).title("Java Programming").build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<BookEntity> bookPage = new PageImpl<>(List.of(bookEntity));

        when(bookRepository.searchBooks(searchTerm, pageable)).thenReturn(bookPage);
        when(bookMapper.mapTo(bookEntity)).thenReturn(bookDto);

        Page<BookDto> result = underTest.searchBooks(searchTerm, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(bookDto);
    }

    @Test
    public void testThatGetPopularBooksReturnsPageOfBooks() {
        BookEntity bookEntity = TestDataUtil.createTestBook();
        BookDto bookDto = BookDto.builder().id(1L).title("Popular Book").build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<BookEntity> bookPage = new PageImpl<>(List.of(bookEntity));

        when(bookRepository.findAllByOrderByAverageRatingDesc(pageable)).thenReturn(bookPage);
        when(bookMapper.mapTo(bookEntity)).thenReturn(bookDto);

        Page<BookDto> result = underTest.getPopularBooks(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(bookDto);
    }

    @Test
    public void testThatIsExistsReturnsFalseWhenNotExists() {
        when(bookRepository.existsById(999L)).thenReturn(false);

        boolean result = underTest.isExists(999L);

        assertThat(result).isFalse();
        verify(bookRepository).existsById(999L);
    }
}
