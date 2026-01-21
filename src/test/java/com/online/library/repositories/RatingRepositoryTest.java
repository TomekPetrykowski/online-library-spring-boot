package com.online.library.repositories;

import com.online.library.domain.entities.BookEntity;
import com.online.library.domain.entities.RatingEntity;
import com.online.library.domain.entities.UserEntity;
import com.online.library.utils.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class RatingRepositoryTest {

    @Autowired
    private RatingRepository underTest;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    public void testThatRatingCanBeCreatedAndRecalled() {
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        RatingEntity rating = TestDataUtil.createTestRating(user, book);
        underTest.save(rating);

        Optional<RatingEntity> result = underTest.findById(rating.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getRating()).isEqualTo(rating.getRating());
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(result.get().getBook().getId()).isEqualTo(book.getId());
    }

    @Test
    public void testThatRatingCanBeDeleted() {
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        RatingEntity rating = TestDataUtil.createTestRating(user, book);
        underTest.save(rating);

        underTest.deleteById(rating.getId());
        Optional<RatingEntity> result = underTest.findById(rating.getId());
        assertThat(result).isNotPresent();
    }

    @Test
    public void testThatFindByBookIdReturnsRatings() {
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        RatingEntity rating = TestDataUtil.createTestRating(user, book);
        underTest.save(rating);

        List<RatingEntity> result = underTest.findByBookId(book.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBook().getId()).isEqualTo(book.getId());
    }

    @Test
    public void testThatFindByUserIdReturnsRatings() {
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        RatingEntity rating = TestDataUtil.createTestRating(user, book);
        underTest.save(rating);

        List<RatingEntity> result = underTest.findByUserId(user.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    public void testThatFindByUserAndBookReturnsRating() {
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        RatingEntity rating = TestDataUtil.createTestRating(user, book);
        underTest.save(rating);

        Optional<RatingEntity> result = underTest.findByUserAndBook(user, book);
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(result.get().getBook().getId()).isEqualTo(book.getId());
    }

    @Test
    public void testThatExistsByUserAndBookReturnsTrueWhenRatingExists() {
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        RatingEntity rating = TestDataUtil.createTestRating(user, book);
        underTest.save(rating);

        boolean result = underTest.existsByUserAndBook(user, book);
        assertThat(result).isTrue();
    }

    @Test
    public void testThatExistsByUserAndBookReturnsFalseWhenNoRating() {
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        boolean result = underTest.existsByUserAndBook(user, book);
        assertThat(result).isFalse();
    }

    @Test
    public void testThatCalculateAverageRatingByBookIdReturnsCorrectAverage() {
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

        RatingEntity rating1 = RatingEntity.builder().user(user1).book(book).rating(4).build();
        RatingEntity rating2 = RatingEntity.builder().user(user2).book(book).rating(5).build();
        underTest.save(rating1);
        underTest.save(rating2);

        Optional<BigDecimal> result = underTest.calculateAverageRatingByBookId(book.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo(new BigDecimal("4.5"));
    }

    @Test
    public void testThatCountRatingsByBookIdReturnsCorrectCount() {
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

        RatingEntity rating1 = TestDataUtil.createTestRating(user1, book);
        RatingEntity rating2 = TestDataUtil.createTestRating(user2, book);
        underTest.save(rating1);
        underTest.save(rating2);

        Long result = underTest.countRatingsByBookId(book.getId());
        assertThat(result).isEqualTo(2L);
    }
}
