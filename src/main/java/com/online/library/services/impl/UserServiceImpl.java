package com.online.library.services.impl;

import com.online.library.domain.dto.UserRequestDto;
import com.online.library.domain.dto.UserResponseDto;
import com.online.library.domain.entities.UserEntity;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.impl.UserMapper;
import com.online.library.repositories.UserRepository;
import com.online.library.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto save(UserRequestDto userDto) {
        log.info("Creating new user: {}", userDto.getUsername());
        UserEntity userEntity = userMapper.mapFromRequest(userDto);
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        UserEntity savedUserEntity = userRepository.save(userEntity);
        log.debug("User created with id: {}", savedUserEntity.getId());
        return userMapper.mapToResponse(savedUserEntity);
    }

    @Override
    public List<UserResponseDto> findAll() {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .map(userMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserResponseDto> findAll(Pageable pageable) {
        Page<UserEntity> foundEntities = userRepository.findAll(pageable);
        return foundEntities.map(userMapper::mapToResponse);
    }

    @Override
    public Optional<UserResponseDto> findById(Long id) {
        return userRepository.findById(id).map(userMapper::mapToResponse);
    }

    @Override
    public Optional<UserResponseDto> findByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::mapToResponse);
    }

    @Override
    public boolean isExists(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public UserResponseDto partialUpdate(Long id, UserRequestDto userDto) {
        userDto.setId(id);

        return userRepository.findById(id).map(existingUser -> {
            Optional.ofNullable(userDto.getUsername()).ifPresent(existingUser::setUsername);
            Optional.ofNullable(userDto.getEmail()).ifPresent(existingUser::setEmail);
            if (userDto.getPassword() != null) {
                existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
            }
            Optional.ofNullable(userDto.getRole()).ifPresent(existingUser::setRole);
            Optional.ofNullable(userDto.getEnabled()).ifPresent(existingUser::setEnabled);
            return userMapper.mapToResponse(userRepository.save(existingUser));
        }).orElseThrow(() -> new ResourceNotFoundException("User does not exist"));
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
