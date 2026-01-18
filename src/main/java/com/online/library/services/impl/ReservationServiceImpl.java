package com.online.library.services.impl;

import com.online.library.domain.dto.ReservationDto;
import com.online.library.domain.entities.ReservationEntity;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.ReservationRepository;
import com.online.library.services.ReservationService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final Mapper<ReservationEntity, ReservationDto> reservationMapper;

    @Override
    public ReservationDto save(ReservationDto reservationDto) {
        ReservationEntity reservationEntity = reservationMapper.mapFrom(reservationDto);
        ReservationEntity savedReservationEntity = reservationRepository.save(reservationEntity);
        return reservationMapper.mapTo(savedReservationEntity);
    }

    @Override
    public List<ReservationDto> findAll() {
        return StreamSupport.stream(reservationRepository.findAll().spliterator(), false)
                .map(reservationMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ReservationDto> findAll(Pageable pageable) {
        Page<ReservationEntity> foundReservations = reservationRepository.findAll(pageable);
        return foundReservations.map(reservationMapper::mapTo);
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
    public ReservationDto partialUpdate(Long id, @NotNull ReservationDto reservationDto) {
        reservationDto.setId(id);

        return reservationRepository.findById(id).map(existingReservation -> {
            Optional.ofNullable(reservationDto.getStatus()).ifPresent(existingReservation::setStatus);
            Optional.ofNullable(reservationDto.getConfirmedAt()).ifPresent(existingReservation::setConfirmedAt);
            Optional.ofNullable(reservationDto.getLoanedAt()).ifPresent(existingReservation::setLoanedAt);
            Optional.ofNullable(reservationDto.getReturnedAt()).ifPresent(existingReservation::setReturnedAt);
            return reservationMapper.mapTo(reservationRepository.save(existingReservation));
        }).orElseThrow(() -> new ResourceNotFoundException("Reservation does not exist"));
    }

    @Override
    public List<ReservationDto> findByUserId(Long userId) {
        return reservationRepository.findByUserId(userId).stream()
                .map(reservationMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }
}
