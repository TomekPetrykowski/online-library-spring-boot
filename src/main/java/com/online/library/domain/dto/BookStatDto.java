package com.online.library.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookStatDto {
    private Long id;
    private String title;
    private BigDecimal averageRating;
    private Long reservationCount;
}
