package com.online.library.mappers.impl;

import com.online.library.domain.dto.ReservationDto;
import com.online.library.domain.entities.ReservationEntity;
import com.online.library.mappers.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationMapper implements Mapper<ReservationEntity, ReservationDto> {

    private final ModelMapper modelMapper;

    @Override
    public ReservationDto mapTo(ReservationEntity reservationEntity) {
        return modelMapper.map(reservationEntity, ReservationDto.class);
    }

    @Override
    public ReservationEntity mapFrom(ReservationDto reservationDto) {
        return modelMapper.map(reservationDto, ReservationEntity.class);
    }
}
