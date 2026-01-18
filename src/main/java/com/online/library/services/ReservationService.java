package com.online.library.services;

import com.online.library.domain.dto.ReservationDto;

import java.util.List;

public interface ReservationService extends BaseService<ReservationDto, Long> {
    List<ReservationDto> findByUserId(Long userId);
}
