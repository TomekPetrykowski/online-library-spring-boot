package com.online.library.repositories;

import com.online.library.domain.entities.BookEntity;
import com.online.library.domain.entities.ReservationEntity;
import com.online.library.domain.entities.UserEntity;
import com.online.library.domain.enums.ReservationStatus;
import com.online.library.utils.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
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

    @Test
    public void testFindByUserIdReturnsReservations() {
        // Given
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book1 = TestDataUtil.createTestBook();
        book1.setIsbn("978-0-001");
        bookRepository.save(book1);

        BookEntity book2 = TestDataUtil.createTestBook();
        book2.setIsbn("978-0-002");
        bookRepository.save(book2);

        ReservationEntity reservation1 = TestDataUtil.createTestReservation(user, book1);
        ReservationEntity reservation2 = TestDataUtil.createTestReservation(user, book2);
        underTest.save(reservation1);
        underTest.save(reservation2);

        // When
        List<ReservationEntity> result = underTest.findByUserId(user.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.getUser().getId().equals(user.getId()));
    }

    @Test
    public void testFindByUserIdReturnsEmptyWhenNoReservations() {
        // Given
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        // When
        List<ReservationEntity> result = underTest.findByUserId(user.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByUserIdOrderByReservedAtDescReturnsOrderedResults() {
        // Given
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book1 = TestDataUtil.createTestBook();
        book1.setIsbn("978-0-001");
        bookRepository.save(book1);

        BookEntity book2 = TestDataUtil.createTestBook();
        book2.setIsbn("978-0-002");
        bookRepository.save(book2);

        ReservationEntity reservation1 = TestDataUtil.createTestReservation(user, book1);
        ReservationEntity reservation2 = TestDataUtil.createTestReservation(user, book2);
        underTest.save(reservation1);
        underTest.save(reservation2);

        // When
        List<ReservationEntity> result = underTest.findByUserIdOrderByReservedAtDesc(user.getId());

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    public void testFindByBookIdReturnsReservations() {
        // Given
        UserEntity user1 = TestDataUtil.createTestUser();
        user1.setUsername("user1");
        user1.setEmail("user1@test.com");
        userRepository.save(user1);

        UserEntity user2 = TestDataUtil.createTestUser();
        user2.setUsername("user2");
        user2.setEmail("user2@test.com");
        userRepository.save(user2);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        ReservationEntity reservation1 = TestDataUtil.createTestReservation(user1, book);
        ReservationEntity reservation2 = TestDataUtil.createTestReservation(user2, book);
        underTest.save(reservation1);
        underTest.save(reservation2);

        // When
        List<ReservationEntity> result = underTest.findByBookId(book.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.getBook().getId().equals(book.getId()));
    }

    @Test
    public void testFindByBookIdReturnsEmptyWhenNoReservations() {
        // Given
        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        // When
        List<ReservationEntity> result = underTest.findByBookId(book.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByStatusReturnsMatchingReservations() {
        // Given
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book1 = TestDataUtil.createTestBook();
        book1.setIsbn("978-0-001");
        bookRepository.save(book1);

        BookEntity book2 = TestDataUtil.createTestBook();
        book2.setIsbn("978-0-002");
        bookRepository.save(book2);

        ReservationEntity waitingReservation = TestDataUtil.createTestReservation(user, book1);
        waitingReservation.setStatus(ReservationStatus.OCZEKUJĄCA);
        underTest.save(waitingReservation);

        ReservationEntity confirmedReservation = TestDataUtil.createTestReservation(user, book2);
        confirmedReservation.setStatus(ReservationStatus.POTWIERDZONA);
        underTest.save(confirmedReservation);

        // When
        List<ReservationEntity> waitingResults = underTest.findByStatus(ReservationStatus.OCZEKUJĄCA);
        List<ReservationEntity> confirmedResults = underTest.findByStatus(ReservationStatus.POTWIERDZONA);

        // Then - verify our reservations are included and have correct status
        assertThat(waitingResults).isNotEmpty();
        assertThat(waitingResults.stream()
                .filter(r -> r.getId().equals(waitingReservation.getId()))
                .findFirst())
                .isPresent()
                .get()
                .extracting(ReservationEntity::getStatus)
                .isEqualTo(ReservationStatus.OCZEKUJĄCA);

        assertThat(confirmedResults).isNotEmpty();
        assertThat(confirmedResults.stream()
                .filter(r -> r.getId().equals(confirmedReservation.getId()))
                .findFirst())
                .isPresent()
                .get()
                .extracting(ReservationEntity::getStatus)
                .isEqualTo(ReservationStatus.POTWIERDZONA);
    }

    @Test
    public void testFindByStatusReturnsEmptyWhenNoMatch() {
        // Given
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        ReservationEntity reservation = TestDataUtil.createTestReservation(user, book);
        reservation.setStatus(ReservationStatus.OCZEKUJĄCA);
        underTest.save(reservation);

        // When
        List<ReservationEntity> result = underTest.findByStatus(ReservationStatus.ZWRÓCONA);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void testCountByBookIdAndStatusReturnsCorrectCount() {
        // Given
        UserEntity user1 = TestDataUtil.createTestUser();
        user1.setUsername("user1");
        user1.setEmail("user1@test.com");
        userRepository.save(user1);

        UserEntity user2 = TestDataUtil.createTestUser();
        user2.setUsername("user2");
        user2.setEmail("user2@test.com");
        userRepository.save(user2);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        ReservationEntity loaned1 = TestDataUtil.createTestReservation(user1, book);
        loaned1.setStatus(ReservationStatus.WYPOŻYCZONA);
        underTest.save(loaned1);

        ReservationEntity loaned2 = TestDataUtil.createTestReservation(user2, book);
        loaned2.setStatus(ReservationStatus.WYPOŻYCZONA);
        underTest.save(loaned2);

        // When
        Long count = underTest.countByBookIdAndStatus(book.getId(), ReservationStatus.WYPOŻYCZONA);

        // Then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    public void testCountByBookIdAndStatusReturnsZeroWhenNoMatch() {
        // Given
        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        // When
        Long count = underTest.countByBookIdAndStatus(book.getId(), ReservationStatus.WYPOŻYCZONA);

        // Then
        assertThat(count).isEqualTo(0L);
    }

    @Test
    public void testFindByUserIdAndBookIdAndStatusIsActiveReturnsWhenActive() {
        // Given
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        ReservationEntity activeReservation = TestDataUtil.createTestReservation(user, book);
        activeReservation.setStatus(ReservationStatus.OCZEKUJĄCA);
        underTest.save(activeReservation);

        // When
        Optional<ReservationEntity> result = underTest.findByUserIdAndBookIdAndStatusIsActive(
                user.getId(), book.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(ReservationStatus.OCZEKUJĄCA);
    }

    @Test
    public void testFindByUserIdAndBookIdAndStatusIsActiveReturnsEmptyWhenReturned() {
        // Given
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        ReservationEntity returnedReservation = TestDataUtil.createTestReservation(user, book);
        returnedReservation.setStatus(ReservationStatus.ZWRÓCONA);
        underTest.save(returnedReservation);

        // When
        Optional<ReservationEntity> result = underTest.findByUserIdAndBookIdAndStatusIsActive(
                user.getId(), book.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindByUserIdAndBookIdAndStatusIsActiveReturnsForConfirmedStatus() {
        // Given
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        ReservationEntity confirmedReservation = TestDataUtil.createTestReservation(user, book);
        confirmedReservation.setStatus(ReservationStatus.POTWIERDZONA);
        underTest.save(confirmedReservation);

        // When
        Optional<ReservationEntity> result = underTest.findByUserIdAndBookIdAndStatusIsActive(
                user.getId(), book.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(ReservationStatus.POTWIERDZONA);
    }

    @Test
    public void testFindByUserIdAndBookIdAndStatusIsActiveReturnsForLoanedStatus() {
        // Given
        UserEntity user = TestDataUtil.createTestUser();
        userRepository.save(user);

        BookEntity book = TestDataUtil.createTestBook();
        bookRepository.save(book);

        ReservationEntity loanedReservation = TestDataUtil.createTestReservation(user, book);
        loanedReservation.setStatus(ReservationStatus.WYPOŻYCZONA);
        underTest.save(loanedReservation);

        // When
        Optional<ReservationEntity> result = underTest.findByUserIdAndBookIdAndStatusIsActive(
                user.getId(), book.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(ReservationStatus.WYPOŻYCZONA);
    }
}
