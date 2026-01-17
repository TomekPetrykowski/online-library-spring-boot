package com.online.library.repositories;

import com.online.library.domain.entities.BookEntity;
import com.online.library.domain.entities.ReservationEntity;
import com.online.library.domain.entities.UserEntity;
import com.online.library.utils.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository underTest;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    public void testThatReservationCanBeCreatedAndRecalled() {
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        ReservationEntity reservation = TestDataUtil.createTestReservation(user, book);
        underTest.save(reservation);

        Optional<ReservationEntity> result = underTest.findById(reservation.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(result.get().getBook().getId()).isEqualTo(book.getId());
        assertThat(result.get().getStatus()).isEqualTo(reservation.getStatus());
    }

    @Test
    public void testThatReservationCanBeDeleted() {
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        ReservationEntity reservation = TestDataUtil.createTestReservation(user, book);
        underTest.save(reservation);

        underTest.deleteById(reservation.getId());
        Optional<ReservationEntity> result = underTest.findById(reservation.getId());
        assertThat(result).isNotPresent();
    }
}
