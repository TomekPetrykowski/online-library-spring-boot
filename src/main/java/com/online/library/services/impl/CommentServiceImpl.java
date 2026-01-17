package com.online.library.services.impl;

import com.online.library.domain.dto.CommentDto;
import com.online.library.domain.entities.CommentEntity;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.CommentRepository;
import com.online.library.services.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final Mapper<CommentEntity, CommentDto> commentMapper;

    @Override
    public CommentDto save(CommentDto commentDto) {
        CommentEntity commentEntity = commentMapper.mapFrom(commentDto);
        CommentEntity savedCommentEntity = commentRepository.save(commentEntity);
        return commentMapper.mapTo(savedCommentEntity);
    }

    @Override
    public List<CommentDto> findAll() {
        return StreamSupport.stream(commentRepository.findAll().spliterator(), false)
                .map(commentMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CommentDto> findById(Long id) {
        return commentRepository.findById(id).map(commentMapper::mapTo);
    }

    @Override
    public boolean isExists(Long id) {
        return commentRepository.existsById(id);
    }

    @Override
    public CommentDto partialUpdate(Long id, CommentDto commentDto) {
        commentDto.setId(id);

        return commentRepository.findById(id).map(existingComment -> {
            Optional.ofNullable(commentDto.getContent()).ifPresent(existingComment::setContent);
            return commentMapper.mapTo(commentRepository.save(existingComment));
        }).orElseThrow(() -> new ResourceNotFoundException("Comment does not exist"));
    }

    @Override
    public void delete(Long id) {
        commentRepository.deleteById(id);
    }
}
