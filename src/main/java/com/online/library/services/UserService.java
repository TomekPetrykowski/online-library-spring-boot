package com.online.library.services;

import com.online.library.domain.dto.UserRequestDto;
import com.online.library.domain.dto.UserResponseDto;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserResponseDto save(UserRequestDto userDto);
    List<UserResponseDto> findAll();
    Optional<UserResponseDto> findById(Long id);
    boolean isExists(Long id);
    UserResponseDto partialUpdate(Long id, UserRequestDto userDto);
    void delete(Long id);
}
