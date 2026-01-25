package com.online.library.services.impl;

import com.online.library.domain.dto.UserRequestDto;
import com.online.library.domain.dto.UserResponseDto;
import com.online.library.domain.entities.UserEntity;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.impl.UserMapper;
import com.online.library.repositories.UserRepository;
import com.online.library.utils.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl underTest;

    @Test
    public void testThatUserIsSavedSuccessfully() {
        UserEntity userEntity = TestDataUtil.createTestUser();
        UserRequestDto userRequestDto = UserRequestDto.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .email(userEntity.getEmail())
                .role(userEntity.getRole())
                .enabled(userEntity.getEnabled())
                .build();
        UserResponseDto userResponseDto = UserResponseDto.builder()
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .role(userEntity.getRole())
                .enabled(userEntity.getEnabled())
                .build();

        when(userMapper.mapFromRequest(userRequestDto)).thenReturn(userEntity);
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encodedPassword");
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.mapToResponse(userEntity)).thenReturn(userResponseDto);

        UserResponseDto result = underTest.save(userRequestDto);

        assertThat(result).isEqualTo(userResponseDto);
        verify(userRepository, times(1)).save(userEntity);
    }

    @Test
    public void testThatFindAllReturnsListOfUsers() {
        UserEntity userEntity = TestDataUtil.createTestUser();
        UserResponseDto userResponseDto = UserResponseDto.builder().id(1L).username("testuser").build();

        when(userRepository.findAll()).thenReturn(List.of(userEntity));
        when(userMapper.mapToResponse(userEntity)).thenReturn(userResponseDto);

        List<UserResponseDto> result = underTest.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(userResponseDto);
    }

    @Test
    public void testThatFindAllWithPageableReturnsPageOfUsers() {
        UserEntity userEntity = TestDataUtil.createTestUser();
        UserResponseDto userResponseDto = UserResponseDto.builder().id(1L).username("testuser").build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> userPage = new PageImpl<>(List.of(userEntity));

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.mapToResponse(userEntity)).thenReturn(userResponseDto);

        Page<UserResponseDto> result = underTest.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(userResponseDto);
    }

    @Test
    public void testThatFindByIdReturnsUserWhenExists() {
        UserEntity userEntity = TestDataUtil.createTestUser();
        userEntity.setId(1L);
        UserResponseDto userResponseDto = UserResponseDto.builder().id(1L).username("testuser").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userMapper.mapToResponse(userEntity)).thenReturn(userResponseDto);

        Optional<UserResponseDto> result = underTest.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(userResponseDto);
    }

    @Test
    public void testThatFindByIdReturnsEmptyWhenNotExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<UserResponseDto> result = underTest.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    public void testThatIsExistsReturnsTrueWhenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        boolean result = underTest.isExists(1L);

        assertThat(result).isTrue();
    }

    @Test
    public void testThatPartialUpdateUpdatesUserSuccessfully() {
        Long userId = 1L;
        UserEntity existingUser = TestDataUtil.createTestUser();
        existingUser.setId(userId);

        UserRequestDto updateDto = UserRequestDto.builder().username("newusername").build();
        UserResponseDto updatedDto = UserResponseDto.builder().id(userId).username("newusername").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.mapToResponse(any(UserEntity.class))).thenReturn(updatedDto);

        UserResponseDto result = underTest.partialUpdate(userId, updateDto);

        assertThat(result.getUsername()).isEqualTo("newusername");
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    public void testThatPartialUpdateThrowsExceptionWhenUserDoesNotExist() {
        Long userId = 1L;
        UserRequestDto updateDto = UserRequestDto.builder().username("newusername").build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.partialUpdate(userId, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User does not exist");
    }

    @Test
    public void testThatDeleteCallsRepository() {
        Long userId = 1L;
        doNothing().when(userRepository).deleteById(userId);

        underTest.delete(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    public void testThatFindByUsernameReturnsUserWhenExists() {
        String username = "testuser";
        UserEntity userEntity = TestDataUtil.createTestUser();
        userEntity.setId(1L);
        userEntity.setUsername(username);
        UserResponseDto userResponseDto = UserResponseDto.builder()
                .id(1L)
                .username(username)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(userMapper.mapToResponse(userEntity)).thenReturn(userResponseDto);

        Optional<UserResponseDto> result = underTest.findByUsername(username);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(username);
        verify(userRepository).findByUsername(username);
    }

    @Test
    public void testThatFindByUsernameReturnsEmptyWhenNotExists() {
        String username = "nonexistent";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Optional<UserResponseDto> result = underTest.findByUsername(username);

        assertThat(result).isEmpty();
        verify(userRepository).findByUsername(username);
    }

    @Test
    public void testThatPartialUpdateEncodesPasswordWhenProvided() {
        Long userId = 1L;
        UserEntity existingUser = TestDataUtil.createTestUser();
        existingUser.setId(userId);
        String newPassword = "newPassword12345";
        String encodedPassword = "encodedNewPassword12345";

        UserRequestDto updateDto = UserRequestDto.builder()
                .password(newPassword)
                .build();
        UserResponseDto updatedDto = UserResponseDto.builder()
                .id(userId)
                .username(existingUser.getUsername())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.mapToResponse(any(UserEntity.class))).thenReturn(updatedDto);

        underTest.partialUpdate(userId, updateDto);

        verify(passwordEncoder).encode(newPassword);
        assertThat(existingUser.getPassword()).isEqualTo(encodedPassword);
        verify(userRepository).save(existingUser);
    }

    @Test
    public void testThatPartialUpdateThrowsExceptionWhenPasswordTooShort() {
        Long userId = 1L;
        UserEntity existingUser = TestDataUtil.createTestUser();
        existingUser.setId(userId);
        String shortPassword = "short"; // Less than 12 characters

        UserRequestDto updateDto = UserRequestDto.builder()
                .password(shortPassword)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> underTest.partialUpdate(userId, updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password must be between 12 and 256 characters");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testThatPartialUpdateDoesNotEncodeEmptyPassword() {
        Long userId = 1L;
        UserEntity existingUser = TestDataUtil.createTestUser();
        existingUser.setId(userId);
        String originalPassword = existingUser.getPassword();

        UserRequestDto updateDto = UserRequestDto.builder()
                .password("")
                .build();
        UserResponseDto updatedDto = UserResponseDto.builder()
                .id(userId)
                .username(existingUser.getUsername())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.mapToResponse(any(UserEntity.class))).thenReturn(updatedDto);

        underTest.partialUpdate(userId, updateDto);

        verify(passwordEncoder, never()).encode(any());
        assertThat(existingUser.getPassword()).isEqualTo(originalPassword);
    }

    @Test
    public void testThatPartialUpdateDoesNotEncodeNullPassword() {
        Long userId = 1L;
        UserEntity existingUser = TestDataUtil.createTestUser();
        existingUser.setId(userId);
        String originalPassword = existingUser.getPassword();

        UserRequestDto updateDto = UserRequestDto.builder()
                .password(null)
                .build();
        UserResponseDto updatedDto = UserResponseDto.builder()
                .id(userId)
                .username(existingUser.getUsername())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.mapToResponse(any(UserEntity.class))).thenReturn(updatedDto);

        underTest.partialUpdate(userId, updateDto);

        verify(passwordEncoder, never()).encode(any());
        assertThat(existingUser.getPassword()).isEqualTo(originalPassword);
    }

    @Test
    public void testThatIsExistsReturnsFalseWhenNotExists() {
        when(userRepository.existsById(999L)).thenReturn(false);

        boolean result = underTest.isExists(999L);

        assertThat(result).isFalse();
        verify(userRepository).existsById(999L);
    }
}
