package com.online.library.services;

import com.online.library.domain.dto.ReservationDto;
import com.online.library.domain.enums.ReservationStatus;

import java.util.List;
import java.util.Optional;

public interface ReservationService extends BaseService<ReservationDto, Long> {

    List<ReservationDto> findByUserId(Long userId);

    List<ReservationDto> findByUserIdOrderByDate(Long userId);

    List<ReservationDto> findByBookId(Long bookId);

    List<ReservationDto> findByStatus(ReservationStatus status);

    ReservationDto createReservation(Long userId, Long bookId);

    ReservationDto changeStatus(Long reservationId, ReservationStatus newStatus);

    void cancelReservation(Long reservationId);

    boolean canUserReserveBook(Long userId, Long bookId);

    boolean hasAvailableCopies(Long bookId);

    Optional<ReservationDto> getActiveReservation(Long userId, Long bookId);
}
