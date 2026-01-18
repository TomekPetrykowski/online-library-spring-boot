package com.online.library.services.impl;

import com.online.library.domain.dto.RatingDto;
import com.online.library.domain.entities.BookEntity;
import com.online.library.domain.entities.RatingEntity;
import com.online.library.domain.entities.UserEntity;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.BookRepository;
import com.online.library.repositories.RatingRepository;
import com.online.library.repositories.UserRepository;
import com.online.library.services.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final Mapper<RatingEntity, RatingDto> ratingMapper;

    private static final int RATING_COOLDOWN_DAYS = 7;

    @Override
    public RatingDto save(RatingDto ratingDto) {
        RatingEntity ratingEntity = ratingMapper.mapFrom(ratingDto);
        RatingEntity savedRatingEntity = ratingRepository.save(ratingEntity);
        return ratingMapper.mapTo(savedRatingEntity);
    }

    @Override
    public List<RatingDto> findAll() {
        return StreamSupport.stream(ratingRepository.findAll().spliterator(), false)
                .map(ratingMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RatingDto> findAll(Pageable pageable) {
        Page<RatingEntity> foundRatings = ratingRepository.findAll(pageable);
        return foundRatings.map(ratingMapper::mapTo);
    }

    @Override
    public Optional<RatingDto> findById(Long id) {
        return ratingRepository.findById(id).map(ratingMapper::mapTo);
    }

    @Override
    public boolean isExists(Long id) {
        return ratingRepository.existsById(id);
    }

    @Override
    public RatingDto partialUpdate(Long id, RatingDto ratingDto) {
        ratingDto.setId(id);

        return ratingRepository.findById(id).map(existingRating -> {
            Optional.ofNullable(ratingDto.getRating()).ifPresent(existingRating::setRating);
            return ratingMapper.mapTo(ratingRepository.save(existingRating));
        }).orElseThrow(() -> new ResourceNotFoundException("Rating does not exist"));
    }

    @Override
    public void delete(Long id) {
        ratingRepository.deleteById(id);
    }

    @Override
    public List<RatingDto> findByBookId(Long bookId) {
        return ratingRepository.findByBookId(bookId).stream()
                .map(ratingMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public List<RatingDto> findByUserId(Long userId) {
        return ratingRepository.findByUserId(userId).stream()
                .map(ratingMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public boolean canUserRateBook(Long userId, Long bookId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(RATING_COOLDOWN_DAYS);
        Optional<RatingEntity> recentRating = ratingRepository.findRecentRatingByUserAndBook(user, book, weekAgo);

        return recentRating.isEmpty();
    }

    @Override
    @Transactional
    public RatingDto addRating(Long userId, Long bookId, Integer rating) {
        if (!canUserRateBook(userId, bookId)) {
            throw new IllegalStateException("User can only rate a book once per week");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        RatingEntity ratingEntity = RatingEntity.builder()
                .user(user)
                .book(book)
                .rating(rating)
                .build();

        RatingEntity savedRating = ratingRepository.save(ratingEntity);

        updateBookAverageRating(bookId);

        return ratingMapper.mapTo(savedRating);
    }

    @Override
    public BigDecimal calculateAverageRating(Long bookId) {
        return ratingRepository.calculateAverageRatingByBookId(bookId)
                .map(avg -> avg.setScale(2, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public Long countRatingsForBook(Long bookId) {
        return ratingRepository.countRatingsByBookId(bookId);
    }

    @Override
    public Integer getUserRatingForBook(Long userId, Long bookId) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        BookEntity book = bookRepository.findById(bookId).orElse(null);

        if (user == null || book == null) {
            return null;
        }

        return ratingRepository.findByUserAndBook(user, book)
                .map(RatingEntity::getRating)
                .orElse(null);
    }

    private void updateBookAverageRating(Long bookId) {
        BigDecimal averageRating = calculateAverageRating(bookId);
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        book.setAverageRating(averageRating);
        bookRepository.save(book);
    }
}
