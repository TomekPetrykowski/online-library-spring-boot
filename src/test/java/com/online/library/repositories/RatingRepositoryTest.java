package com.online.library.repositories;

import com.online.library.domain.entities.BookEntity;
import com.online.library.domain.entities.RatingEntity;
import com.online.library.domain.entities.UserEntity;
import com.online.library.utils.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

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
}
