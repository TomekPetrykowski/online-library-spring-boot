package com.online.library.repositories;

import com.online.library.domain.entities.ReservationEntity;
import com.online.library.domain.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

        List<ReservationEntity> findByUserId(Long userId);

        List<ReservationEntity> findByUserIdOrderByReservedAtDesc(Long userId);

        List<ReservationEntity> findByBookId(Long bookId);

        List<ReservationEntity> findByStatus(ReservationStatus status);

        Long countByBookIdAndStatus(Long bookId, ReservationStatus status);

        @Query("SELECT r FROM ReservationEntity r WHERE r.user.id = :userId AND r.book.id = :bookId " +
                        "AND r.status IN ('OCZEKUJĄCA', 'POTWIERDZONA', 'WYPOŻYCZONA')")
        Optional<ReservationEntity> findByUserIdAndBookIdAndStatusIsActive(
                        @Param("userId") Long userId,
                        @Param("bookId") Long bookId);
}
