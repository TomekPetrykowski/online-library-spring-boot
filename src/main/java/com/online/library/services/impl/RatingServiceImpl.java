package com.online.library.services.impl;

import com.online.library.domain.dto.RatingDto;
import com.online.library.domain.entities.RatingEntity;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.RatingRepository;
import com.online.library.services.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final Mapper<RatingEntity, RatingDto> ratingMapper;

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
}
