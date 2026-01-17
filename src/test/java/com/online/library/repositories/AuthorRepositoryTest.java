package com.online.library.repositories;

import com.online.library.domain.entities.AuthorEntity;
import com.online.library.utils.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository underTest;

    @Test
    public void testThatAuthorCanBeCreatedAndRecalled() {
        AuthorEntity author = TestDataUtil.createTestAuthor();
        underTest.save(author);
        Optional<AuthorEntity> result = underTest.findById(author.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(author.getName());
        assertThat(result.get().getLastName()).isEqualTo(author.getLastName());
    }

    @Test
    public void testThatAuthorCanBeUpdated() {
        AuthorEntity author = TestDataUtil.createTestAuthor();
        underTest.save(author);
        author.setName("Updated Name");
        underTest.save(author);
        Optional<AuthorEntity> result = underTest.findById(author.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Updated Name");
    }

    @Test
    public void testThatAuthorCanBeDeleted() {
        AuthorEntity author = TestDataUtil.createTestAuthor();
        underTest.save(author);
        underTest.deleteById(author.getId());
        Optional<AuthorEntity> result = underTest.findById(author.getId());
        assertThat(result).isNotPresent();
    }
}
