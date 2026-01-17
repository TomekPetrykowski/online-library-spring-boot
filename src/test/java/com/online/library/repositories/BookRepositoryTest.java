package com.online.library.repositories;

import com.online.library.domain.entities.BookEntity;
import com.online.library.utils.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    private BookRepository underTest;

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
}
