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
import com.online.library.services.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final Mapper<ReservationEntity, ReservationDto> reservationMapper;

    @Override
    public ReservationDto save(ReservationDto reservationDto) {
        ReservationEntity entity = reservationMapper.mapFrom(reservationDto);
        return reservationMapper.mapTo(reservationRepository.save(entity));
    }

    @Override
    public Page<ReservationDto> findAll(Pageable pageable) {
        return reservationRepository.findAll(pageable).map(reservationMapper::mapTo);
    }

    @Override
    public Optional<ReservationDto> findById(Long id) {
        return reservationRepository.findById(id).map(reservationMapper::mapTo);
    }

    @Override
    public boolean isExists(Long id) {
        return reservationRepository.existsById(id);
    }

    @Override
    public ReservationDto partialUpdate(Long id, ReservationDto dto) {
        return reservationRepository.findById(id).map(existing -> {
            Optional.ofNullable(dto.getStatus()).ifPresent(existing::setStatus);
            Optional.ofNullable(dto.getConfirmedAt()).ifPresent(existing::setConfirmedAt);
            Optional.ofNullable(dto.getLoanedAt()).ifPresent(existing::setLoanedAt);
            Optional.ofNullable(dto.getReturnedAt()).ifPresent(existing::setReturnedAt);
            return reservationMapper.mapTo(reservationRepository.save(existing));
        }).orElseThrow(() -> new ResourceNotFoundException("Reservation does not exist"));
    }

    @Override
    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }

    @Override
    public List<ReservationDto> findByUserId(Long userId) {
        return reservationRepository.findByUserId(userId).stream()
                .map(reservationMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> findByUserIdOrderByDate(Long userId) {
        return reservationRepository.findByUserIdOrderByReservedAtDesc(userId).stream()
                .map(reservationMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> findByBookId(Long bookId) {
        return reservationRepository.findByBookId(bookId).stream()
                .map(reservationMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> findByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status).stream()
                .map(reservationMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReservationDto createReservation(Long userId, Long bookId) {
        log.info("Creating reservation for user={} book={}", userId, bookId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (getActiveReservation(userId, bookId).isPresent()) {
            throw new IllegalStateException("User already has an active reservation for this book");
        }

        if (!hasAvailableCopies(bookId)) {
            throw new IllegalStateException("No copies available for reservation");
        }

        ReservationEntity reservation = ReservationEntity.builder()
                .user(user)
                .book(book)
                .status(ReservationStatus.OCZEKUJĄCA)
                .build();

        return reservationMapper.mapTo(reservationRepository.save(reservation));
    }

    @Override
    @Transactional
    public ReservationDto changeStatus(Long reservationId, ReservationStatus newStatus) {
        log.info("Changing reservation {} to status {}", reservationId, newStatus);

        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        ReservationStatus currentStatus = reservation.getStatus();

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot transition from %s to %s", currentStatus, newStatus));
        }

        // Handle book copies when loaning or returning
        if (newStatus == ReservationStatus.WYPOŻYCZONA) {
            adjustBookCopies(reservation.getBook(), -1);
        } else if (newStatus == ReservationStatus.ZWRÓCONA) {
            adjustBookCopies(reservation.getBook(), +1);
        }

        // Set timestamps and status
        reservation.setStatus(newStatus);
        switch (newStatus) {
            case POTWIERDZONA -> reservation.setConfirmedAt(LocalDateTime.now());
            case WYPOŻYCZONA -> reservation.setLoanedAt(LocalDateTime.now());
            case ZWRÓCONA -> reservation.setReturnedAt(LocalDateTime.now());
            default -> {
            }
        }

        return reservationMapper.mapTo(reservationRepository.save(reservation));
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        if (!reservation.getStatus().canBeCancelled()) {
            throw new IllegalStateException(
                    "Cannot cancel reservation in status " + reservation.getStatus());
        }

        reservationRepository.delete(reservation);
    }

    @Override
    public boolean canUserReserveBook(Long userId, Long bookId) {
        return getActiveReservation(userId, bookId).isEmpty() && hasAvailableCopies(bookId);
    }

    @Override
    public boolean hasAvailableCopies(Long bookId) {
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        Integer copies = book.getCopiesAvailable();
        if (copies == null || copies <= 0) {
            return false;
        }

        Long loanedCount = reservationRepository.countByBookIdAndStatus(bookId, ReservationStatus.WYPOŻYCZONA);
        return copies > loanedCount;
    }

    @Override
    public Optional<ReservationDto> getActiveReservation(Long userId, Long bookId) {
        return reservationRepository.findByUserIdAndBookIdAndStatusIsActive(userId, bookId)
                .map(reservationMapper::mapTo);
    }

    private void adjustBookCopies(BookEntity book, int delta) {
        int current = book.getCopiesAvailable() != null ? book.getCopiesAvailable() : 0;
        int newValue = current + delta;
        if (newValue < 0) {
            throw new IllegalStateException("No copies available");
        }
        book.setCopiesAvailable(newValue);
        bookRepository.save(book);
    }
}
