package com.online.library.repositories;

import com.online.library.domain.entities.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    List<CommentEntity> findByBookIdOrderByCreatedAtDesc(Long bookId);

    Page<CommentEntity> findByBookIdOrderByCreatedAtDesc(Long bookId, Pageable pageable);

    List<CommentEntity> findByUserId(Long userId);

    Long countByBookId(Long bookId);
}
