package com.online.library.repositories;

import com.online.library.domain.entities.BookEntity;
import com.online.library.domain.entities.CommentEntity;
import com.online.library.domain.entities.UserEntity;
import com.online.library.utils.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
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

    @Test
    public void testThatFindByBookIdOrderByCreatedAtDescReturnsComments() {
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        CommentEntity comment = TestDataUtil.createTestComment(user, book);
        underTest.save(comment);

        List<CommentEntity> result = underTest.findByBookIdOrderByCreatedAtDesc(book.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBook().getId()).isEqualTo(book.getId());
    }

    @Test
    public void testThatFindByBookIdWithPageableReturnsComments() {
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        CommentEntity comment = TestDataUtil.createTestComment(user, book);
        underTest.save(comment);

        Page<CommentEntity> result = underTest.findByBookIdOrderByCreatedAtDesc(book.getId(), PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBook().getId()).isEqualTo(book.getId());
    }

    @Test
    public void testThatFindByUserIdReturnsComments() {
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        CommentEntity comment = TestDataUtil.createTestComment(user, book);
        underTest.save(comment);

        List<CommentEntity> result = underTest.findByUserId(user.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    public void testThatCountByBookIdReturnsCorrectCount() {
        UserEntity user1 = TestDataUtil.createTestUser();
        userRepository.save(user1);

        UserEntity user2 = UserEntity.builder()
                .username("testuser2")
                .password("password")
                .email("test2@example.com")
                .role(user1.getRole())
                .enabled(true)
                .build();
        userRepository.save(user2);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        CommentEntity comment1 = TestDataUtil.createTestComment(user1, book);
        CommentEntity comment2 = CommentEntity.builder()
                .user(user2)
                .book(book)
                .content("Another comment")
                .build();
        underTest.save(comment1);
        underTest.save(comment2);

        Long result = underTest.countByBookId(book.getId());
        assertThat(result).isEqualTo(2L);
    }
}
