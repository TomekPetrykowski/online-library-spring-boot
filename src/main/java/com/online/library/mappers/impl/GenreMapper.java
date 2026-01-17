package com.online.library.mappers.impl;

import com.online.library.domain.dto.GenreDto;
import com.online.library.domain.entities.GenreEntity;
import com.online.library.mappers.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenreMapper implements Mapper<GenreEntity, GenreDto> {

    private final ModelMapper modelMapper;

    @Override
    public GenreDto mapTo(GenreEntity genreEntity) {
        return modelMapper.map(genreEntity, GenreDto.class);
    }

    @Override
    public GenreEntity mapFrom(GenreDto genreDto) {
        return modelMapper.map(genreDto, GenreEntity.class);
    }
}
