package com.online.library.repositories;

import com.online.library.domain.entities.AuthorEntity;
import com.online.library.domain.entities.BookEntity;
import com.online.library.domain.entities.GenreEntity;
import com.online.library.utils.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookRepositoryTest {

    @Autowired
    private BookRepository underTest;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Test
    public void testThatBookCanBeCreatedAndRecalled() {
        BookEntity bookEntity = TestDataUtil.createTestBook();
        underTest.save(bookEntity);
        Optional<BookEntity> result = underTest.findById(bookEntity.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo(bookEntity.getTitle());
    }

    @Test
    public void testThatFindByIsbnReturnsBook() {
        BookEntity bookEntity = TestDataUtil.createTestBook();
        underTest.save(bookEntity);
        Optional<BookEntity> result = underTest.findByIsbn(bookEntity.getIsbn());
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo(bookEntity.getTitle());
    }

    @Test
    public void testThatBookCanBeDeleted() {
        BookEntity bookEntity = TestDataUtil.createTestBook();
        underTest.save(bookEntity);
        underTest.deleteById(bookEntity.getId());
        Optional<BookEntity> result = underTest.findById(bookEntity.getId());
        assertThat(result).isNotPresent();
    }

    @Test
    public void testThatSearchBooksReturnsFilteredBooks() {
        AuthorEntity author = authorRepository.save(TestDataUtil.createTestAuthor());
        GenreEntity genre = genreRepository.save(TestDataUtil.createTestGenre());

        BookEntity book1 = TestDataUtil.createTestBook();
        book1.setTitle("Java Programming");
        book1.setAuthors(Set.of(author));
        book1.setGenres(Set.of(genre));
        underTest.save(book1);

        BookEntity book2 = TestDataUtil.createTestBook();
        book2.setTitle("Spring in Action");
        book2.setIsbn("1234567890");
        underTest.save(book2);

        Page<BookEntity> resultByTitle = underTest.searchBooks("Java", PageRequest.of(0, 10));
        assertThat(resultByTitle.getContent()).hasSize(1);
        assertThat(resultByTitle.getContent().get(0).getTitle()).isEqualTo("Java Programming");

        Page<BookEntity> resultByAuthor = underTest.searchBooks(author.getLastName(), PageRequest.of(0, 10));
        assertThat(resultByAuthor.getContent()).hasSize(1);
        assertThat(resultByAuthor.getContent().get(0).getTitle()).isEqualTo("Java Programming");

        Page<BookEntity> resultByGenre = underTest.searchBooks(genre.getName(), PageRequest.of(0, 10));
        assertThat(resultByGenre.getContent()).hasSize(1);
        assertThat(resultByGenre.getContent().get(0).getTitle()).isEqualTo("Java Programming");
    }

    @Test
    public void testThatFindAllByOrderByAverageRatingDescReturnsSortedBooks() {
        BookEntity book1 = TestDataUtil.createTestBook();
        book1.setTitle("Book 1");
        book1.setAverageRating(new BigDecimal("3.5"));
        book1.setIsbn("isbn1");
        underTest.save(book1);

        BookEntity book2 = TestDataUtil.createTestBook();
        book2.setTitle("Book 2");
        book2.setAverageRating(new BigDecimal("4.8"));
        book2.setIsbn("isbn2");
        underTest.save(book2);

        Page<BookEntity> result = underTest.findAllByOrderByAverageRatingDesc(PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
        
        // Find the indices of our test books in the result
        int index1 = -1;
        int index2 = -1;
        for (int i = 0; i < result.getContent().size(); i++) {
            if (result.getContent().get(i).getTitle().equals("Book 1")) index1 = i;
            if (result.getContent().get(i).getTitle().equals("Book 2")) index2 = i;
        }
        
        assertThat(index1).isGreaterThan(-1);
        assertThat(index2).isGreaterThan(-1);
        assertThat(index2).isLessThan(index1);
    }
}
