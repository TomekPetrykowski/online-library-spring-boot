package com.online.library.services.impl;

import com.online.library.domain.dto.ReservationDto;
import com.online.library.domain.entities.BookEntity;
import com.online.library.domain.entities.ReservationEntity;
import com.online.library.domain.entities.UserEntity;
import com.online.library.domain.enums.ReservationStatus;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.BookRepository;
import com.online.library.repositories.ReservationRepository;
import com.online.library.repositories.UserRepository;
import com.online.library.utils.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private Mapper<ReservationEntity, ReservationDto> reservationMapper;

    @InjectMocks
    private ReservationServiceImpl underTest;

    @Test
    public void testThatReservationIsSavedSuccessfully() {
        ReservationEntity reservationEntity = TestDataUtil.createTestReservation(null, null);
        ReservationDto reservationDto = ReservationDto.builder().status(reservationEntity.getStatus()).build();

        when(reservationMapper.mapFrom(reservationDto)).thenReturn(reservationEntity);
        when(reservationRepository.save(reservationEntity)).thenReturn(reservationEntity);
        when(reservationMapper.mapTo(reservationEntity)).thenReturn(reservationDto);

        ReservationDto result = underTest.save(reservationDto);

        assertThat(result).isEqualTo(reservationDto);
        verify(reservationRepository, times(1)).save(reservationEntity);
    }

    @Test
    public void testThatFindAllWithPageableReturnsPageOfReservations() {
        ReservationEntity reservationEntity = TestDataUtil.createTestReservation(null, null);
        ReservationDto reservationDto = ReservationDto.builder().id(1L).status(ReservationStatus.OCZEKUJĄCA).build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ReservationEntity> reservationPage = new PageImpl<>(List.of(reservationEntity));

        when(reservationRepository.findAll(pageable)).thenReturn(reservationPage);
        when(reservationMapper.mapTo(reservationEntity)).thenReturn(reservationDto);

        Page<ReservationDto> result = underTest.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(reservationDto);
    }

    @Test
    public void testThatFindByIdReturnsReservationWhenExists() {
        ReservationEntity reservationEntity = TestDataUtil.createTestReservation(null, null);
        reservationEntity.setId(1L);
        ReservationDto reservationDto = ReservationDto.builder().id(1L).status(ReservationStatus.OCZEKUJĄCA).build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservationEntity));
        when(reservationMapper.mapTo(reservationEntity)).thenReturn(reservationDto);

        Optional<ReservationDto> result = underTest.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(reservationDto);
    }

    @Test
    public void testThatFindByIdReturnsEmptyWhenNotExists() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<ReservationDto> result = underTest.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    public void testThatIsExistsReturnsTrueWhenExists() {
        when(reservationRepository.existsById(1L)).thenReturn(true);

        boolean result = underTest.isExists(1L);

        assertThat(result).isTrue();
    }

    @Test
    public void testThatPartialUpdateUpdatesReservationSuccessfully() {
        Long reservationId = 1L;
        ReservationEntity existingReservation = TestDataUtil.createTestReservation(null, null);
        existingReservation.setId(reservationId);

        ReservationDto updateDto = ReservationDto.builder().status(ReservationStatus.POTWIERDZONA).build();
        ReservationDto updatedDto = ReservationDto.builder().id(reservationId).status(ReservationStatus.POTWIERDZONA)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(existingReservation));
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(existingReservation);
        when(reservationMapper.mapTo(any(ReservationEntity.class))).thenReturn(updatedDto);

        ReservationDto result = underTest.partialUpdate(reservationId, updateDto);

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.POTWIERDZONA);
        verify(reservationRepository, times(1)).save(existingReservation);
    }

    @Test
    public void testThatPartialUpdateThrowsExceptionWhenReservationDoesNotExist() {
        Long reservationId = 1L;
        ReservationDto updateDto = ReservationDto.builder().status(ReservationStatus.POTWIERDZONA).build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.partialUpdate(reservationId, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Reservation does not exist");
    }

    @Test
    public void testThatDeleteCallsRepository() {
        Long reservationId = 1L;
        doNothing().when(reservationRepository).deleteById(reservationId);

        underTest.delete(reservationId);

        verify(reservationRepository, times(1)).deleteById(reservationId);
    }

    @Test
    public void testCreateReservationSuccessfully() {
        // Given
        Long userId = 1L;
        Long bookId = 2L;
        UserEntity user = TestDataUtil.createTestUser();
        user.setId(userId);
        BookEntity book = TestDataUtil.createTestBook();
        book.setId(bookId);
        book.setCopiesAvailable(5);

        ReservationEntity savedReservation = TestDataUtil.createTestReservation(user, book);
        savedReservation.setId(1L);

        ReservationDto expectedDto = ReservationDto.builder()
                .id(1L)
                .status(ReservationStatus.OCZEKUJĄCA)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book)); // called in createReservation AND
                                                                             // hasAvailableCopies
        when(reservationRepository.findByUserIdAndBookIdAndStatusIsActive(userId, bookId))
                .thenReturn(Optional.empty());
        when(reservationRepository.countByBookIdAndStatus(bookId, ReservationStatus.WYPOŻYCZONA))
                .thenReturn(0L);
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(savedReservation);
        when(reservationMapper.mapTo(savedReservation)).thenReturn(expectedDto);

        // When
        ReservationDto result = underTest.createReservation(userId, bookId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.OCZEKUJĄCA);
        verify(userRepository).findById(userId);
        verify(bookRepository, times(2)).findById(bookId); // called twice: createReservation + hasAvailableCopies
        verify(reservationRepository).save(any(ReservationEntity.class));
    }

    @Test
    public void testCreateReservationThrowsWhenUserNotFound() {
        // Given
        Long userId = 999L;
        Long bookId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> underTest.createReservation(userId, bookId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(userId);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    public void testCreateReservationThrowsWhenBookNotFound() {
        // Given
        Long userId = 1L;
        Long bookId = 999L;
        UserEntity user = TestDataUtil.createTestUser();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> underTest.createReservation(userId, bookId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    public void testCreateReservationThrowsWhenUserHasActiveReservation() {
        // Given
        Long userId = 1L;
        Long bookId = 2L;
        UserEntity user = TestDataUtil.createTestUser();
        user.setId(userId);
        BookEntity book = TestDataUtil.createTestBook();
        book.setId(bookId);

        ReservationEntity activeReservation = TestDataUtil.createTestReservation(user, book);
        ReservationDto activeReservationDto = ReservationDto.builder()
                .id(1L)
                .status(ReservationStatus.OCZEKUJĄCA)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(reservationRepository.findByUserIdAndBookIdAndStatusIsActive(userId, bookId))
                .thenReturn(Optional.of(activeReservation));
        when(reservationMapper.mapTo(activeReservation)).thenReturn(activeReservationDto);

        // When/Then
        assertThatThrownBy(() -> underTest.createReservation(userId, bookId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already has an active reservation");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    public void testCreateReservationThrowsWhenNoCopiesAvailable() {
        // Given
        Long userId = 1L;
        Long bookId = 2L;
        UserEntity user = TestDataUtil.createTestUser();
        user.setId(userId);
        BookEntity book = TestDataUtil.createTestBook();
        book.setId(bookId);
        book.setCopiesAvailable(0);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(reservationRepository.findByUserIdAndBookIdAndStatusIsActive(userId, bookId))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> underTest.createReservation(userId, bookId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No copies available");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    public void testChangeStatusFromOczekujacaToPotwierdzona() {
        // Given
        Long reservationId = 1L;
        UserEntity user = TestDataUtil.createTestUser();
        BookEntity book = TestDataUtil.createTestBook();
        ReservationEntity reservation = TestDataUtil.createTestReservation(user, book);
        reservation.setId(reservationId);
        reservation.setStatus(ReservationStatus.OCZEKUJĄCA);

        ReservationDto expectedDto = ReservationDto.builder()
                .id(reservationId)
                .status(ReservationStatus.POTWIERDZONA)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(reservation);
        when(reservationMapper.mapTo(any(ReservationEntity.class))).thenReturn(expectedDto);

        // When
        ReservationDto result = underTest.changeStatus(reservationId, ReservationStatus.POTWIERDZONA);

        // Then
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.POTWIERDZONA);
        verify(reservationRepository).save(any(ReservationEntity.class));
        verify(bookRepository, never()).save(any());
    }

    @Test
    public void testChangeStatusFromPotwierdzonaToWypozyczonaDecrementsCopies() {
        // Given
        Long reservationId = 1L;
        UserEntity user = TestDataUtil.createTestUser();
        BookEntity book = TestDataUtil.createTestBook();
        book.setCopiesAvailable(5);
        ReservationEntity reservation = TestDataUtil.createTestReservation(user, book);
        reservation.setId(reservationId);
        reservation.setStatus(ReservationStatus.POTWIERDZONA);

        ReservationDto expectedDto = ReservationDto.builder()
                .id(reservationId)
                .status(ReservationStatus.WYPOŻYCZONA)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(reservation);
        when(bookRepository.save(any(BookEntity.class))).thenReturn(book);
        when(reservationMapper.mapTo(any(ReservationEntity.class))).thenReturn(expectedDto);

        // When
        ReservationDto result = underTest.changeStatus(reservationId, ReservationStatus.WYPOŻYCZONA);

        // Then
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.WYPOŻYCZONA);
        assertThat(book.getCopiesAvailable()).isEqualTo(4);
        verify(bookRepository).save(book);
        verify(reservationRepository).save(any(ReservationEntity.class));
    }

    @Test
    public void testChangeStatusFromWypozyczonaToZwroconaIncrementsCopies() {
        // Given
        Long reservationId = 1L;
        UserEntity user = TestDataUtil.createTestUser();
        BookEntity book = TestDataUtil.createTestBook();
        book.setCopiesAvailable(4);
        ReservationEntity reservation = TestDataUtil.createTestReservation(user, book);
        reservation.setId(reservationId);
        reservation.setStatus(ReservationStatus.WYPOŻYCZONA);

        ReservationDto expectedDto = ReservationDto.builder()
                .id(reservationId)
                .status(ReservationStatus.ZWRÓCONA)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(reservation);
        when(bookRepository.save(any(BookEntity.class))).thenReturn(book);
        when(reservationMapper.mapTo(any(ReservationEntity.class))).thenReturn(expectedDto);

        // When
        ReservationDto result = underTest.changeStatus(reservationId, ReservationStatus.ZWRÓCONA);

        // Then
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.ZWRÓCONA);
        assertThat(book.getCopiesAvailable()).isEqualTo(5);
        verify(bookRepository).save(book);
    }

    @Test
    public void testChangeStatusInvalidTransitionThrowsException() {
        // Given
        Long reservationId = 1L;
        UserEntity user = TestDataUtil.createTestUser();
        BookEntity book = TestDataUtil.createTestBook();
        ReservationEntity reservation = TestDataUtil.createTestReservation(user, book);
        reservation.setId(reservationId);
        reservation.setStatus(ReservationStatus.OCZEKUJĄCA);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // When/Then - cannot go directly from OCZEKUJĄCA to WYPOŻYCZONA
        assertThatThrownBy(() -> underTest.changeStatus(reservationId, ReservationStatus.WYPOŻYCZONA))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transition");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    public void testChangeStatusReservationNotFoundThrowsException() {
        // Given
        Long reservationId = 999L;
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> underTest.changeStatus(reservationId, ReservationStatus.POTWIERDZONA))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Reservation not found");
    }

    @Test
    public void testCancelReservationSuccessfullyWhenOczekujaca() {
        // Given
        Long reservationId = 1L;
        UserEntity user = TestDataUtil.createTestUser();
        BookEntity book = TestDataUtil.createTestBook();
        ReservationEntity reservation = TestDataUtil.createTestReservation(user, book);
        reservation.setId(reservationId);
        reservation.setStatus(ReservationStatus.OCZEKUJĄCA);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        doNothing().when(reservationRepository).delete(reservation);

        // When
        underTest.cancelReservation(reservationId);

        // Then
        verify(reservationRepository).delete(reservation);
    }

    @Test
    public void testCancelReservationSuccessfullyWhenPotwierdzona() {
        // Given
        Long reservationId = 1L;
        UserEntity user = TestDataUtil.createTestUser();
        BookEntity book = TestDataUtil.createTestBook();
        ReservationEntity reservation = TestDataUtil.createTestReservation(user, book);
        reservation.setId(reservationId);
        reservation.setStatus(ReservationStatus.POTWIERDZONA);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        doNothing().when(reservationRepository).delete(reservation);

        // When
        underTest.cancelReservation(reservationId);

        // Then
        verify(reservationRepository).delete(reservation);
    }

    @Test
    public void testCancelReservationThrowsWhenWypozyczona() {
        // Given
        Long reservationId = 1L;
        UserEntity user = TestDataUtil.createTestUser();
        BookEntity book = TestDataUtil.createTestBook();
        ReservationEntity reservation = TestDataUtil.createTestReservation(user, book);
        reservation.setId(reservationId);
        reservation.setStatus(ReservationStatus.WYPOŻYCZONA);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // When/Then
        assertThatThrownBy(() -> underTest.cancelReservation(reservationId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel reservation");

        verify(reservationRepository, never()).delete(any());
    }

    @Test
    public void testCancelReservationNotFoundThrowsException() {
        // Given
        Long reservationId = 999L;
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> underTest.cancelReservation(reservationId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Reservation not found");
    }

    @Test
    public void testCanUserReserveBookReturnsTrue() {
        // Given
        Long userId = 1L;
        Long bookId = 2L;
        BookEntity book = TestDataUtil.createTestBook();
        book.setId(bookId);
        book.setCopiesAvailable(5);

        when(reservationRepository.findByUserIdAndBookIdAndStatusIsActive(userId, bookId))
                .thenReturn(Optional.empty());
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(reservationRepository.countByBookIdAndStatus(bookId, ReservationStatus.WYPOŻYCZONA))
                .thenReturn(2L);

        // When
        boolean result = underTest.canUserReserveBook(userId, bookId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void testCanUserReserveBookReturnsFalseWhenHasActiveReservation() {
        // Given
        Long userId = 1L;
        Long bookId = 2L;
        UserEntity user = TestDataUtil.createTestUser();
        BookEntity book = TestDataUtil.createTestBook();
        ReservationEntity activeReservation = TestDataUtil.createTestReservation(user, book);
        ReservationDto activeReservationDto = ReservationDto.builder()
                .id(1L)
                .status(ReservationStatus.OCZEKUJĄCA)
                .build();

        when(reservationRepository.findByUserIdAndBookIdAndStatusIsActive(userId, bookId))
                .thenReturn(Optional.of(activeReservation));
        when(reservationMapper.mapTo(activeReservation)).thenReturn(activeReservationDto);

        // When
        boolean result = underTest.canUserReserveBook(userId, bookId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void testCanUserReserveBookReturnsFalseWhenNoCopies() {
        // Given
        Long userId = 1L;
        Long bookId = 2L;
        BookEntity book = TestDataUtil.createTestBook();
        book.setId(bookId);
        book.setCopiesAvailable(0);

        when(reservationRepository.findByUserIdAndBookIdAndStatusIsActive(userId, bookId))
                .thenReturn(Optional.empty());
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // When
        boolean result = underTest.canUserReserveBook(userId, bookId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void testHasAvailableCopiesReturnsTrue() {
        // Given
        Long bookId = 1L;
        BookEntity book = TestDataUtil.createTestBook();
        book.setId(bookId);
        book.setCopiesAvailable(5);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(reservationRepository.countByBookIdAndStatus(bookId, ReservationStatus.WYPOŻYCZONA))
                .thenReturn(2L);

        // When
        boolean result = underTest.hasAvailableCopies(bookId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void testHasAvailableCopiesReturnsFalseWhenAllLoaned() {
        // Given
        Long bookId = 1L;
        BookEntity book = TestDataUtil.createTestBook();
        book.setId(bookId);
        book.setCopiesAvailable(3);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(reservationRepository.countByBookIdAndStatus(bookId, ReservationStatus.WYPOŻYCZONA))
                .thenReturn(3L);

        // When
        boolean result = underTest.hasAvailableCopies(bookId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void testHasAvailableCopiesReturnsFalseWhenZeroCopies() {
        // Given
        Long bookId = 1L;
        BookEntity book = TestDataUtil.createTestBook();
        book.setId(bookId);
        book.setCopiesAvailable(0);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // When
        boolean result = underTest.hasAvailableCopies(bookId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void testHasAvailableCopiesThrowsWhenBookNotFound() {
        // Given
        Long bookId = 999L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> underTest.hasAvailableCopies(bookId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found");
    }

    @Test
    public void testFindByUserIdReturnsReservations() {
        // Given
        Long userId = 1L;
        UserEntity user = TestDataUtil.createTestUser();
        user.setId(userId);
        ReservationEntity reservation = TestDataUtil.createTestReservation(user, null);
        ReservationDto dto = ReservationDto.builder().id(1L).build();

        when(reservationRepository.findByUserId(userId)).thenReturn(List.of(reservation));
        when(reservationMapper.mapTo(reservation)).thenReturn(dto);

        // When
        List<ReservationDto> result = underTest.findByUserId(userId);

        // Then
        assertThat(result).hasSize(1);
        verify(reservationRepository).findByUserId(userId);
    }

    @Test
    public void testFindByBookIdReturnsReservations() {
        // Given
        Long bookId = 1L;
        BookEntity book = TestDataUtil.createTestBook();
        book.setId(bookId);
        ReservationEntity reservation = TestDataUtil.createTestReservation(null, book);
        ReservationDto dto = ReservationDto.builder().id(1L).build();

        when(reservationRepository.findByBookId(bookId)).thenReturn(List.of(reservation));
        when(reservationMapper.mapTo(reservation)).thenReturn(dto);

        // When
        List<ReservationDto> result = underTest.findByBookId(bookId);

        // Then
        assertThat(result).hasSize(1);
        verify(reservationRepository).findByBookId(bookId);
    }

    @Test
    public void testFindByStatusReturnsReservations() {
        // Given
        ReservationStatus status = ReservationStatus.OCZEKUJĄCA;
        ReservationEntity reservation = TestDataUtil.createTestReservation(null, null);
        ReservationDto dto = ReservationDto.builder().id(1L).status(status).build();

        when(reservationRepository.findByStatus(status)).thenReturn(List.of(reservation));
        when(reservationMapper.mapTo(reservation)).thenReturn(dto);

        // When
        List<ReservationDto> result = underTest.findByStatus(status);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(status);
        verify(reservationRepository).findByStatus(status);
    }

    @Test
    public void testFindByUserIdOrderByDateReturnsReservationsSorted() {
        // Given
        Long userId = 1L;
        ReservationEntity reservation = TestDataUtil.createTestReservation(null, null);
        ReservationDto dto = ReservationDto.builder().id(1L).build();

        when(reservationRepository.findByUserIdOrderByReservedAtDesc(userId)).thenReturn(List.of(reservation));
        when(reservationMapper.mapTo(reservation)).thenReturn(dto);

        // When
        List<ReservationDto> result = underTest.findByUserIdOrderByDate(userId);

        // Then
        assertThat(result).hasSize(1);
        verify(reservationRepository).findByUserIdOrderByReservedAtDesc(userId);
    }

    @Test
    public void testGetActiveReservationReturnsWhenExists() {
        // Given
        Long userId = 1L;
        Long bookId = 2L;
        ReservationEntity reservation = TestDataUtil.createTestReservation(null, null);
        ReservationDto dto = ReservationDto.builder().id(1L).build();

        when(reservationRepository.findByUserIdAndBookIdAndStatusIsActive(userId, bookId))
                .thenReturn(Optional.of(reservation));
        when(reservationMapper.mapTo(reservation)).thenReturn(dto);

        // When
        Optional<ReservationDto> result = underTest.getActiveReservation(userId, bookId);

        // Then
        assertThat(result).isPresent();
        verify(reservationRepository).findByUserIdAndBookIdAndStatusIsActive(userId, bookId);
    }

    @Test
    public void testGetActiveReservationReturnsEmptyWhenNotExists() {
        // Given
        Long userId = 1L;
        Long bookId = 2L;

        when(reservationRepository.findByUserIdAndBookIdAndStatusIsActive(userId, bookId))
                .thenReturn(Optional.empty());

        // When
        Optional<ReservationDto> result = underTest.getActiveReservation(userId, bookId);

        // Then
        assertThat(result).isEmpty();
        verify(reservationRepository).findByUserIdAndBookIdAndStatusIsActive(userId, bookId);
    }
}