package com.online.library.mappers.impl;

import com.online.library.domain.dto.UserRequestDto;
import com.online.library.domain.dto.UserResponseDto;
import com.online.library.domain.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final ModelMapper modelMapper;

    public UserResponseDto mapToResponse(UserEntity userEntity) {
        return modelMapper.map(userEntity, UserResponseDto.class);
    }

    public UserEntity mapFromRequest(UserRequestDto userRequestDto) {
        return modelMapper.map(userRequestDto, UserEntity.class);
    }
}
