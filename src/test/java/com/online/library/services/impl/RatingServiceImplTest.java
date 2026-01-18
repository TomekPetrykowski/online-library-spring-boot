package com.online.library.services.impl;

import com.online.library.domain.dto.RatingDto;
import com.online.library.domain.entities.RatingEntity;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.RatingRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingServiceImplTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private Mapper<RatingEntity, RatingDto> ratingMapper;

    @InjectMocks
    private RatingServiceImpl underTest;

    @Test
    public void testThatRatingIsSavedSuccessfully() {
        RatingEntity ratingEntity = TestDataUtil.createTestRating(null, null);
        RatingDto ratingDto = RatingDto.builder().rating(ratingEntity.getRating()).build();

        when(ratingMapper.mapFrom(ratingDto)).thenReturn(ratingEntity);
        when(ratingRepository.save(ratingEntity)).thenReturn(ratingEntity);
        when(ratingMapper.mapTo(ratingEntity)).thenReturn(ratingDto);

        RatingDto result = underTest.save(ratingDto);

        assertThat(result).isEqualTo(ratingDto);
        verify(ratingRepository, times(1)).save(ratingEntity);
    }

    @Test
    public void testThatFindAllReturnsListOfRatings() {
        RatingEntity ratingEntity = TestDataUtil.createTestRating(null, null);
        RatingDto ratingDto = RatingDto.builder().id(1L).rating(5).build();

        when(ratingRepository.findAll()).thenReturn(List.of(ratingEntity));
        when(ratingMapper.mapTo(ratingEntity)).thenReturn(ratingDto);

        List<RatingDto> result = underTest.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(ratingDto);
    }

    @Test
    public void testThatFindAllWithPageableReturnsPageOfRatings() {
        RatingEntity ratingEntity = TestDataUtil.createTestRating(null, null);
        RatingDto ratingDto = RatingDto.builder().id(1L).rating(5).build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<RatingEntity> ratingPage = new PageImpl<>(List.of(ratingEntity));

        when(ratingRepository.findAll(pageable)).thenReturn(ratingPage);
        when(ratingMapper.mapTo(ratingEntity)).thenReturn(ratingDto);

        Page<RatingDto> result = underTest.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(ratingDto);
    }

    @Test
    public void testThatFindByIdReturnsRatingWhenExists() {
        RatingEntity ratingEntity = TestDataUtil.createTestRating(null, null);
        ratingEntity.setId(1L);
        RatingDto ratingDto = RatingDto.builder().id(1L).rating(5).build();

        when(ratingRepository.findById(1L)).thenReturn(Optional.of(ratingEntity));
        when(ratingMapper.mapTo(ratingEntity)).thenReturn(ratingDto);

        Optional<RatingDto> result = underTest.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(ratingDto);
    }

    @Test
    public void testThatFindByIdReturnsEmptyWhenNotExists() {
        when(ratingRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<RatingDto> result = underTest.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    public void testThatIsExistsReturnsTrueWhenExists() {
        when(ratingRepository.existsById(1L)).thenReturn(true);

        boolean result = underTest.isExists(1L);

        assertThat(result).isTrue();
    }

    @Test
    public void testThatPartialUpdateUpdatesRatingSuccessfully() {
        Long ratingId = 1L;
        RatingEntity existingRating = TestDataUtil.createTestRating(null, null);
        existingRating.setId(ratingId);

        RatingDto updateDto = RatingDto.builder().rating(4).build();
        RatingDto updatedDto = RatingDto.builder().id(ratingId).rating(4).build();

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(existingRating));
        when(ratingRepository.save(any(RatingEntity.class))).thenReturn(existingRating);
        when(ratingMapper.mapTo(any(RatingEntity.class))).thenReturn(updatedDto);

        RatingDto result = underTest.partialUpdate(ratingId, updateDto);

        assertThat(result.getRating()).isEqualTo(4);
        verify(ratingRepository, times(1)).save(existingRating);
    }

    @Test
    public void testThatPartialUpdateThrowsExceptionWhenRatingDoesNotExist() {
        Long ratingId = 1L;
        RatingDto updateDto = RatingDto.builder().rating(4).build();

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.partialUpdate(ratingId, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Rating does not exist");
    }

    @Test
    public void testThatDeleteCallsRepository() {
        Long ratingId = 1L;
        doNothing().when(ratingRepository).deleteById(ratingId);

        underTest.delete(ratingId);

        verify(ratingRepository, times(1)).deleteById(ratingId);
    }
}
