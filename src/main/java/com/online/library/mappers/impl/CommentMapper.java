package com.online.library.mappers.impl;

import com.online.library.domain.dto.CommentDto;
import com.online.library.domain.entities.CommentEntity;
import com.online.library.mappers.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentMapper implements Mapper<CommentEntity, CommentDto> {

    private final ModelMapper modelMapper;

    @Override
    public CommentDto mapTo(CommentEntity commentEntity) {
        return modelMapper.map(commentEntity, CommentDto.class);
    }

    @Override
    public CommentEntity mapFrom(CommentDto commentDto) {
        return modelMapper.map(commentDto, CommentEntity.class);
    }
}
