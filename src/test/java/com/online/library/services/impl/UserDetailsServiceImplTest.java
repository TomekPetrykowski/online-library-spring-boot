package com.online.library.services.impl;

import com.online.library.domain.entities.UserEntity;
import com.online.library.domain.enums.UserRole;
import com.online.library.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl underTest;

    @Test
    void testLoadUserByUsernameSuccessfully() {
        // Given
        String username = "testuser";
        UserEntity userEntity = UserEntity.builder()
                .id(1L)
                .username(username)
                .password("encodedPassword")
                .email("test@example.com")
                .role(UserRole.USER)
                .enabled(true)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

        // When
        UserDetails result = underTest.loadUserByUsername(username);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.isAccountNonExpired()).isTrue();
        assertThat(result.isCredentialsNonExpired()).isTrue();
        assertThat(result.isAccountNonLocked()).isTrue();

        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testLoadUserByUsernameNotFoundThrowsException() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> underTest.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with username: " + username);

        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testLoadUserByUsernameReturnsCorrectAuthoritiesForUser() {
        // Given
        String username = "regularuser";
        UserEntity userEntity = UserEntity.builder()
                .id(1L)
                .username(username)
                .password("password")
                .email("user@example.com")
                .role(UserRole.USER)
                .enabled(true)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

        // When
        UserDetails result = underTest.loadUserByUsername(username);

        // Then
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");

        verify(userRepository).findByUsername(username);
    }

    @Test
    void testLoadUserByUsernameReturnsCorrectAuthoritiesForAdmin() {
        // Given
        String username = "adminuser";
        UserEntity userEntity = UserEntity.builder()
                .id(2L)
                .username(username)
                .password("adminpassword")
                .email("admin@example.com")
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

        // When
        UserDetails result = underTest.loadUserByUsername(username);

        // Then
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");

        verify(userRepository).findByUsername(username);
    }

    @Test
    void testLoadUserByUsernameWithDisabledUser() {
        // Given
        String username = "disableduser";
        UserEntity userEntity = UserEntity.builder()
                .id(3L)
                .username(username)
                .password("password")
                .email("disabled@example.com")
                .role(UserRole.USER)
                .enabled(false)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

        // When
        UserDetails result = underTest.loadUserByUsername(username);

        // Then
        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getUsername()).isEqualTo(username);

        verify(userRepository).findByUsername(username);
    }

    @Test
    void testLoadUserByUsernameWithNullEnabledDefaultsToTrue() {
        // Given
        String username = "nullenableduser";
        UserEntity userEntity = UserEntity.builder()
                .id(4L)
                .username(username)
                .password("password")
                .email("null@example.com")
                .role(UserRole.USER)
                .enabled(null)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

        // When
        UserDetails result = underTest.loadUserByUsername(username);

        // Then
        assertThat(result.isEnabled()).isTrue();

        verify(userRepository).findByUsername(username);
    }
}
