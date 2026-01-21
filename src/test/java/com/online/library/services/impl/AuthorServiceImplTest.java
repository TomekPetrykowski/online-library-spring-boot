package com.online.library.services.impl;

import com.online.library.domain.dto.AuthorDto;
import com.online.library.domain.entities.AuthorEntity;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.AuthorRepository;
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
public class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private Mapper<AuthorEntity, AuthorDto> authorMapper;

    @InjectMocks
    private AuthorServiceImpl underTest;

    @Test
    public void testThatAuthorIsSavedSuccessfully() {
        AuthorEntity authorEntity = TestDataUtil.createTestAuthor();
        AuthorDto authorDto = AuthorDto.builder()
                .name(authorEntity.getName())
                .lastName(authorEntity.getLastName())
                .bio(authorEntity.getBio())
                .build();

        when(authorMapper.mapFrom(authorDto)).thenReturn(authorEntity);
        when(authorRepository.save(authorEntity)).thenReturn(authorEntity);
        when(authorMapper.mapTo(authorEntity)).thenReturn(authorDto);

        AuthorDto result = underTest.save(authorDto);

        assertThat(result).isEqualTo(authorDto);
        verify(authorRepository, times(1)).save(authorEntity);
    }

    @Test
    public void testThatFindAllWithPageableReturnsPageOfAuthors() {
        AuthorEntity authorEntity = TestDataUtil.createTestAuthor();
        AuthorDto authorDto = AuthorDto.builder().id(1L).name("H.P.").build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuthorEntity> authorPage = new PageImpl<>(List.of(authorEntity));

        when(authorRepository.findAll(pageable)).thenReturn(authorPage);
        when(authorMapper.mapTo(authorEntity)).thenReturn(authorDto);

        Page<AuthorDto> result = underTest.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(authorDto);
    }

    @Test
    public void testThatFindByIdReturnsAuthorWhenExists() {
        AuthorEntity authorEntity = TestDataUtil.createTestAuthor();
        authorEntity.setId(1L);
        AuthorDto authorDto = AuthorDto.builder().id(1L).name("H.P.").build();

        when(authorRepository.findById(1L)).thenReturn(Optional.of(authorEntity));
        when(authorMapper.mapTo(authorEntity)).thenReturn(authorDto);

        Optional<AuthorDto> result = underTest.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(authorDto);
    }

    @Test
    public void testThatFindByIdReturnsEmptyWhenNotExists() {
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<AuthorDto> result = underTest.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    public void testThatIsExistsReturnsTrueWhenExists() {
        when(authorRepository.existsById(1L)).thenReturn(true);

        boolean result = underTest.isExists(1L);

        assertThat(result).isTrue();
    }

    @Test
    public void testThatPartialUpdateUpdatesAuthorSuccessfully() {
        Long authorId = 1L;
        AuthorEntity existingAuthor = TestDataUtil.createTestAuthor();
        existingAuthor.setId(authorId);

        AuthorDto updateDto = AuthorDto.builder()
                .name("New Name")
                .build();

        AuthorDto updatedDto = AuthorDto.builder()
                .id(authorId)
                .name("New Name")
                .lastName(existingAuthor.getLastName())
                .bio(existingAuthor.getBio())
                .build();

        when(authorRepository.findById(authorId)).thenReturn(Optional.of(existingAuthor));
        when(authorRepository.save(any(AuthorEntity.class))).thenReturn(existingAuthor);
        when(authorMapper.mapTo(any(AuthorEntity.class))).thenReturn(updatedDto);

        AuthorDto result = underTest.partialUpdate(authorId, updateDto);

        assertThat(result.getName()).isEqualTo("New Name");
        verify(authorRepository, times(1)).save(existingAuthor);
    }

    @Test
    public void testThatPartialUpdateThrowsExceptionWhenAuthorDoesNotExist() {
        Long authorId = 1L;
        AuthorDto updateDto = AuthorDto.builder().name("New Name").build();

        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.partialUpdate(authorId, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Author does not exist");
    }

    @Test
    public void testThatDeleteCallsRepository() {
        Long authorId = 1L;
        doNothing().when(authorRepository).deleteById(authorId);

        underTest.delete(authorId);

        verify(authorRepository, times(1)).deleteById(authorId);
    }
}
