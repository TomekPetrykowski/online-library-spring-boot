package com.online.library.services.impl;

import com.online.library.domain.dto.CommentDto;
import com.online.library.domain.entities.BookEntity;
import com.online.library.domain.entities.CommentEntity;
import com.online.library.domain.entities.UserEntity;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.BookRepository;
import com.online.library.repositories.CommentRepository;
import com.online.library.repositories.UserRepository;
import com.online.library.services.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final Mapper<CommentEntity, CommentDto> commentMapper;

    @Override
    public CommentDto save(CommentDto commentDto) {
        CommentEntity commentEntity = commentMapper.mapFrom(commentDto);
        CommentEntity savedCommentEntity = commentRepository.save(commentEntity);
        return commentMapper.mapTo(savedCommentEntity);
    }

    @Override
    public Page<CommentDto> findAll(Pageable pageable) {
        Page<CommentEntity> foundComments = commentRepository.findAll(pageable);
        return foundComments.map(commentMapper::mapTo);
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

    @Override
    public Page<CommentDto> findByBookId(Long bookId, Pageable pageable) {
        return commentRepository.findByBookIdOrderByCreatedAtDesc(bookId, pageable)
                .map(commentMapper::mapTo);
    }

    @Override
    public List<CommentDto> findByUserId(Long userId) {
        return commentRepository.findByUserId(userId).stream()
                .map(commentMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long userId, Long bookId, String content) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        CommentEntity comment = CommentEntity.builder()
                .user(user)
                .book(book)
                .content(content)
                .build();

        CommentEntity savedComment = commentRepository.save(comment);
        return commentMapper.mapTo(savedComment);
    }

    @Override
    public Long countCommentsForBook(Long bookId) {
        return commentRepository.countByBookId(bookId);
    }
}
