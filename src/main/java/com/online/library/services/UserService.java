package com.online.library.services;

import com.online.library.domain.dto.UserRequestDto;
import com.online.library.domain.dto.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserResponseDto save(UserRequestDto userDto);
    List<UserResponseDto> findAll();
    Page<UserResponseDto> findAll(Pageable pageable);
    Optional<UserResponseDto> findById(Long id);
    Optional<UserResponseDto> findByUsername(String username);
    boolean isExists(Long id);
    UserResponseDto partialUpdate(Long id, UserRequestDto userDto);
    void delete(Long id);
}
