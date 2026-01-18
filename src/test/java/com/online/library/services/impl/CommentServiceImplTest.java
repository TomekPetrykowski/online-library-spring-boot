package com.online.library.services.impl;

import com.online.library.domain.dto.CommentDto;
import com.online.library.domain.entities.CommentEntity;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.CommentRepository;
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
public class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private Mapper<CommentEntity, CommentDto> commentMapper;

    @InjectMocks
    private CommentServiceImpl underTest;

    @Test
    public void testThatCommentIsSavedSuccessfully() {
        CommentEntity commentEntity = TestDataUtil.createTestComment(null, null);
        CommentDto commentDto = CommentDto.builder().content(commentEntity.getContent()).build();

        when(commentMapper.mapFrom(commentDto)).thenReturn(commentEntity);
        when(commentRepository.save(commentEntity)).thenReturn(commentEntity);
        when(commentMapper.mapTo(commentEntity)).thenReturn(commentDto);

        CommentDto result = underTest.save(commentDto);

        assertThat(result).isEqualTo(commentDto);
        verify(commentRepository, times(1)).save(commentEntity);
    }

    @Test
    public void testThatFindAllReturnsListOfComments() {
        CommentEntity commentEntity = TestDataUtil.createTestComment(null, null);
        CommentDto commentDto = CommentDto.builder().id(1L).content("Great book!").build();

        when(commentRepository.findAll()).thenReturn(List.of(commentEntity));
        when(commentMapper.mapTo(commentEntity)).thenReturn(commentDto);

        List<CommentDto> result = underTest.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(commentDto);
    }

    @Test
    public void testThatFindAllWithPageableReturnsPageOfComments() {
        CommentEntity commentEntity = TestDataUtil.createTestComment(null, null);
        CommentDto commentDto = CommentDto.builder().id(1L).content("Great book!").build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<CommentEntity> commentPage = new PageImpl<>(List.of(commentEntity));

        when(commentRepository.findAll(pageable)).thenReturn(commentPage);
        when(commentMapper.mapTo(commentEntity)).thenReturn(commentDto);

        Page<CommentDto> result = underTest.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(commentDto);
    }

    @Test
    public void testThatFindByIdReturnsCommentWhenExists() {
        CommentEntity commentEntity = TestDataUtil.createTestComment(null, null);
        commentEntity.setId(1L);
        CommentDto commentDto = CommentDto.builder().id(1L).content("Great book!").build();

        when(commentRepository.findById(1L)).thenReturn(Optional.of(commentEntity));
        when(commentMapper.mapTo(commentEntity)).thenReturn(commentDto);

        Optional<CommentDto> result = underTest.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(commentDto);
    }

    @Test
    public void testThatFindByIdReturnsEmptyWhenNotExists() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<CommentDto> result = underTest.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    public void testThatIsExistsReturnsTrueWhenExists() {
        when(commentRepository.existsById(1L)).thenReturn(true);

        boolean result = underTest.isExists(1L);

        assertThat(result).isTrue();
    }

    @Test
    public void testThatPartialUpdateUpdatesCommentSuccessfully() {
        Long commentId = 1L;
        CommentEntity existingComment = TestDataUtil.createTestComment(null, null);
        existingComment.setId(commentId);

        CommentDto updateDto = CommentDto.builder().content("New content").build();
        CommentDto updatedDto = CommentDto.builder().id(commentId).content("New content").build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));
        when(commentRepository.save(any(CommentEntity.class))).thenReturn(existingComment);
        when(commentMapper.mapTo(any(CommentEntity.class))).thenReturn(updatedDto);

        CommentDto result = underTest.partialUpdate(commentId, updateDto);

        assertThat(result.getContent()).isEqualTo("New content");
        verify(commentRepository, times(1)).save(existingComment);
    }

    @Test
    public void testThatPartialUpdateThrowsExceptionWhenCommentDoesNotExist() {
        Long commentId = 1L;
        CommentDto updateDto = CommentDto.builder().content("New content").build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.partialUpdate(commentId, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Comment does not exist");
    }

    @Test
    public void testThatDeleteCallsRepository() {
        Long commentId = 1L;
        doNothing().when(commentRepository).deleteById(commentId);

        underTest.delete(commentId);

        verify(commentRepository, times(1)).deleteById(commentId);
    }
}
