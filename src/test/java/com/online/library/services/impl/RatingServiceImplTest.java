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

import java.math.BigDecimal;
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
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

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

    @Test
    public void testThatFindByBookIdReturnsListOfRatings() {
        Long bookId = 1L;
        RatingEntity ratingEntity = TestDataUtil.createTestRating(null, null);
        RatingDto ratingDto = RatingDto.builder().id(1L).rating(5).build();

        when(ratingRepository.findByBookId(bookId)).thenReturn(List.of(ratingEntity));
        when(ratingMapper.mapTo(ratingEntity)).thenReturn(ratingDto);

        List<RatingDto> result = underTest.findByBookId(bookId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(ratingDto);
    }

    @Test
    public void testThatFindByUserIdReturnsListOfRatings() {
        Long userId = 1L;
        RatingEntity ratingEntity = TestDataUtil.createTestRating(null, null);
        RatingDto ratingDto = RatingDto.builder().id(1L).rating(5).build();

        when(ratingRepository.findByUserId(userId)).thenReturn(List.of(ratingEntity));
        when(ratingMapper.mapTo(ratingEntity)).thenReturn(ratingDto);

        List<RatingDto> result = underTest.findByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(ratingDto);
    }

    @Test
    public void testThatRateBookCreatesNewRatingWhenNoExisting() {
        Long userId = 1L;
        Long bookId = 1L;
        Integer ratingValue = 4;
        UserEntity user = TestDataUtil.createTestUser();
        user.setId(userId);
        BookEntity book = TestDataUtil.createTestBook();
        book.setId(bookId);
        RatingEntity savedRating = TestDataUtil.createTestRating(user, book);
        savedRating.setId(1L);
        savedRating.setRating(ratingValue);
        RatingDto ratingDto = RatingDto.builder().id(1L).rating(ratingValue).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(ratingRepository.findByUserAndBook(user, book)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(RatingEntity.class))).thenReturn(savedRating);
        when(ratingRepository.calculateAverageRatingByBookId(bookId)).thenReturn(Optional.of(new BigDecimal("4.00")));
        when(ratingMapper.mapTo(savedRating)).thenReturn(ratingDto);

        RatingDto result = underTest.rateBook(userId, bookId, ratingValue);

        assertThat(result).isEqualTo(ratingDto);
        verify(ratingRepository, times(1)).save(any(RatingEntity.class));
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    public void testThatRateBookUpdatesExistingRating() {
        Long userId = 1L;
        Long bookId = 1L;
        Integer newRatingValue = 5;
        UserEntity user = TestDataUtil.createTestUser();
        user.setId(userId);
        BookEntity book = TestDataUtil.createTestBook();
        book.setId(bookId);
        RatingEntity existingRating = TestDataUtil.createTestRating(user, book);
        existingRating.setId(1L);
        existingRating.setRating(3);
        RatingDto ratingDto = RatingDto.builder().id(1L).rating(newRatingValue).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(ratingRepository.findByUserAndBook(user, book)).thenReturn(Optional.of(existingRating));
        when(ratingRepository.save(existingRating)).thenReturn(existingRating);
        when(ratingRepository.calculateAverageRatingByBookId(bookId)).thenReturn(Optional.of(new BigDecimal("5.00")));
        when(ratingMapper.mapTo(existingRating)).thenReturn(ratingDto);

        RatingDto result = underTest.rateBook(userId, bookId, newRatingValue);

        assertThat(result.getRating()).isEqualTo(newRatingValue);
        assertThat(existingRating.getRating()).isEqualTo(newRatingValue);
        verify(ratingRepository, times(1)).save(existingRating);
    }

    @Test
    public void testThatRateBookThrowsExceptionWhenUserNotFound() {
        Long userId = 1L;
        Long bookId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.rateBook(userId, bookId, 5))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    public void testThatRateBookThrowsExceptionWhenBookNotFound() {
        Long userId = 1L;
        Long bookId = 1L;
        UserEntity user = TestDataUtil.createTestUser();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.rateBook(userId, bookId, 5))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Book not found");
    }

    @Test
    public void testThatCalculateAverageRatingReturnsCorrectAverage() {
        Long bookId = 1L;
        BigDecimal expectedAverage = new BigDecimal("4.50");

        when(ratingRepository.calculateAverageRatingByBookId(bookId))
                .thenReturn(Optional.of(expectedAverage));

        BigDecimal result = underTest.calculateAverageRating(bookId);

        assertThat(result).isEqualByComparingTo(expectedAverage);
    }

    @Test
    public void testThatCalculateAverageRatingReturnsZeroWhenNoRatings() {
        Long bookId = 1L;

        when(ratingRepository.calculateAverageRatingByBookId(bookId))
                .thenReturn(Optional.empty());

        BigDecimal result = underTest.calculateAverageRating(bookId);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void testThatCountRatingsForBookReturnsCorrectCount() {
        Long bookId = 1L;
        Long expectedCount = 10L;

        when(ratingRepository.countRatingsByBookId(bookId)).thenReturn(expectedCount);

        Long result = underTest.countRatingsForBook(bookId);

        assertThat(result).isEqualTo(expectedCount);
    }

    @Test
    public void testThatGetUserRatingForBookReturnsRatingWhenExists() {
        Long userId = 1L;
        Long bookId = 1L;
        UserEntity user = TestDataUtil.createTestUser();
        user.setId(userId);
        BookEntity book = TestDataUtil.createTestBook();
        book.setId(bookId);
        RatingEntity rating = TestDataUtil.createTestRating(user, book);
        rating.setRating(4);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(ratingRepository.findByUserAndBook(user, book)).thenReturn(Optional.of(rating));

        Integer result = underTest.getUserRatingForBook(userId, bookId);

        assertThat(result).isEqualTo(4);
    }

    @Test
    public void testThatGetUserRatingForBookReturnsNullWhenNoRating() {
        Long userId = 1L;
        Long bookId = 1L;
        UserEntity user = TestDataUtil.createTestUser();
        user.setId(userId);
        BookEntity book = TestDataUtil.createTestBook();
        book.setId(bookId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(ratingRepository.findByUserAndBook(user, book)).thenReturn(Optional.empty());

        Integer result = underTest.getUserRatingForBook(userId, bookId);

        assertThat(result).isNull();
    }
}
