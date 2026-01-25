package com.online.library.services.impl;

import com.online.library.domain.dto.GenreDto;
import com.online.library.domain.entities.GenreEntity;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.GenreRepository;
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
public class GenreServiceImplTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private Mapper<GenreEntity, GenreDto> genreMapper;

    @InjectMocks
    private GenreServiceImpl underTest;

    @Test
    public void testThatGenreIsSavedSuccessfully() {
        GenreEntity genreEntity = TestDataUtil.createTestGenre();
        GenreDto genreDto = GenreDto.builder().name(genreEntity.getName()).build();

        when(genreMapper.mapFrom(genreDto)).thenReturn(genreEntity);
        when(genreRepository.save(genreEntity)).thenReturn(genreEntity);
        when(genreMapper.mapTo(genreEntity)).thenReturn(genreDto);

        GenreDto result = underTest.save(genreDto);

        assertThat(result).isEqualTo(genreDto);
        verify(genreRepository, times(1)).save(genreEntity);
    }

    @Test
    public void testThatFindAllWithPageableReturnsPageOfGenres() {
        GenreEntity genreEntity = TestDataUtil.createTestGenre();
        GenreDto genreDto = GenreDto.builder().id(1L).name("Horror").build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<GenreEntity> genrePage = new PageImpl<>(List.of(genreEntity));

        when(genreRepository.findAll(pageable)).thenReturn(genrePage);
        when(genreMapper.mapTo(genreEntity)).thenReturn(genreDto);

        Page<GenreDto> result = underTest.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(genreDto);
    }

    @Test
    public void testThatFindByIdReturnsGenreWhenExists() {
        GenreEntity genreEntity = TestDataUtil.createTestGenre();
        genreEntity.setId(1L);
        GenreDto genreDto = GenreDto.builder().id(1L).name("Horror").build();

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genreEntity));
        when(genreMapper.mapTo(genreEntity)).thenReturn(genreDto);

        Optional<GenreDto> result = underTest.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(genreDto);
    }

    @Test
    public void testThatFindByIdReturnsEmptyWhenNotExists() {
        when(genreRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<GenreDto> result = underTest.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    public void testThatIsExistsReturnsTrueWhenExists() {
        when(genreRepository.existsById(1L)).thenReturn(true);

        boolean result = underTest.isExists(1L);

        assertThat(result).isTrue();
    }

    @Test
    public void testThatPartialUpdateUpdatesGenreSuccessfully() {
        Long genreId = 1L;
        GenreEntity existingGenre = TestDataUtil.createTestGenre();
        existingGenre.setId(genreId);

        GenreDto updateDto = GenreDto.builder().name("Sci-Fi").build();
        GenreDto updatedDto = GenreDto.builder().id(genreId).name("Sci-Fi").build();

        when(genreRepository.findById(genreId)).thenReturn(Optional.of(existingGenre));
        when(genreRepository.save(any(GenreEntity.class))).thenReturn(existingGenre);
        when(genreMapper.mapTo(any(GenreEntity.class))).thenReturn(updatedDto);

        GenreDto result = underTest.partialUpdate(genreId, updateDto);

        assertThat(result.getName()).isEqualTo("Sci-Fi");
        verify(genreRepository, times(1)).save(existingGenre);
    }

    @Test
    public void testThatPartialUpdateThrowsExceptionWhenGenreDoesNotExist() {
        Long genreId = 1L;
        GenreDto updateDto = GenreDto.builder().name("Sci-Fi").build();

        when(genreRepository.findById(genreId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.partialUpdate(genreId, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Genre does not exist");
    }

    @Test
    public void testThatDeleteCallsRepository() {
        Long genreId = 1L;
        doNothing().when(genreRepository).deleteById(genreId);

        underTest.delete(genreId);

        verify(genreRepository, times(1)).deleteById(genreId);
    }

    @Test
    public void testThatIsExistsReturnsFalseWhenNotExists() {
        when(genreRepository.existsById(999L)).thenReturn(false);

        boolean result = underTest.isExists(999L);

        assertThat(result).isFalse();
        verify(genreRepository).existsById(999L);
    }
}
