package com.online.library.domain.dto;

import com.online.library.domain.enums.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDto {

    private Long id;

    @NotNull(message = "User is required")
    private UserResponseDto user;

    @NotNull(message = "Book is required")
    private BookDto book;

    @NotNull(message = "Status is required")
    private ReservationStatus status;

    private LocalDateTime reservedAt;

    private LocalDateTime confirmedAt;

    private LocalDateTime loanedAt;

    private LocalDateTime returnedAt;
}
