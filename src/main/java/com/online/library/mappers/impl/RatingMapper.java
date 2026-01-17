package com.online.library.mappers.impl;

import com.online.library.domain.dto.RatingDto;
import com.online.library.domain.entities.RatingEntity;
import com.online.library.mappers.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RatingMapper implements Mapper<RatingEntity, RatingDto> {

    private final ModelMapper modelMapper;

    @Override
    public RatingDto mapTo(RatingEntity ratingEntity) {
        return modelMapper.map(ratingEntity, RatingDto.class);
    }

    @Override
    public RatingEntity mapFrom(RatingDto ratingDto) {
        return modelMapper.map(ratingDto, RatingEntity.class);
    }
}
