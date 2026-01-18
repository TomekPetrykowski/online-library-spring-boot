package com.online.library.services;

import com.online.library.domain.dto.CommentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService extends BaseService<CommentDto, Long> {

    List<CommentDto> findByBookId(Long bookId);

    Page<CommentDto> findByBookId(Long bookId, Pageable pageable);

    List<CommentDto> findByUserId(Long userId);

    CommentDto addComment(Long userId, Long bookId, String content);

    Long countCommentsForBook(Long bookId);
}
