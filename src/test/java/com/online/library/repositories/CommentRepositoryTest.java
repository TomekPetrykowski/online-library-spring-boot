package com.online.library.repositories;

import com.online.library.domain.entities.BookEntity;
import com.online.library.domain.entities.CommentEntity;
import com.online.library.domain.entities.UserEntity;
import com.online.library.utils.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CommentRepositoryTest {

    @Autowired
    private CommentRepository underTest;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    public void testThatCommentCanBeCreatedAndRecalled() {
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        CommentEntity comment = TestDataUtil.createTestComment(user, book);
        underTest.save(comment);

        Optional<CommentEntity> result = underTest.findById(comment.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getContent()).isEqualTo(comment.getContent());
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(result.get().getBook().getId()).isEqualTo(book.getId());
    }

    @Test
    public void testThatCommentCanBeDeleted() {
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        CommentEntity comment = TestDataUtil.createTestComment(user, book);
        underTest.save(comment);

        underTest.deleteById(comment.getId());
        Optional<CommentEntity> result = underTest.findById(comment.getId());
        assertThat(result).isNotPresent();
    }
}
