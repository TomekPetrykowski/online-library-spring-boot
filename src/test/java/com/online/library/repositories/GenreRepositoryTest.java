package com.online.library.repositories;

import com.online.library.domain.entities.GenreEntity;
import com.online.library.utils.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class GenreRepositoryTest {

    @Autowired
    private GenreRepository underTest;

    @Test
    public void testThatGenreCanBeCreatedAndRecalled() {
        GenreEntity genre = TestDataUtil.createTestGenre();
        underTest.save(genre);
        Optional<GenreEntity> result = underTest.findById(genre.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(genre.getName());
    }

    @Test
    public void testThatFindByNameReturnsGenre() {
        GenreEntity genre = TestDataUtil.createTestGenre();
        underTest.save(genre);
        Optional<GenreEntity> result = underTest.findByName(genre.getName());
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(genre.getName());
    }

    @Test
    public void testThatGenreCanBeDeleted() {
        GenreEntity genre = TestDataUtil.createTestGenre();
        underTest.save(genre);
        underTest.deleteById(genre.getId());
        Optional<GenreEntity> result = underTest.findById(genre.getId());
        assertThat(result).isNotPresent();
    }
}
