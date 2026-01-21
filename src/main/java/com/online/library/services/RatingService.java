package com.online.library.services;

import com.online.library.domain.dto.RatingDto;

import java.math.BigDecimal;
import java.util.List;

public interface RatingService extends BaseService<RatingDto, Long> {

    List<RatingDto> findByBookId(Long bookId);

    List<RatingDto> findByUserId(Long userId);

    RatingDto rateBook(Long userId, Long bookId, Integer rating);

    BigDecimal calculateAverageRating(Long bookId);

    Long countRatingsForBook(Long bookId);

    Integer getUserRatingForBook(Long userId, Long bookId);
}
