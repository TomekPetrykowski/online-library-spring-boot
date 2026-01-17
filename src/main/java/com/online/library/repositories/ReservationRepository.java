package com.online.library.repositories;

import com.online.library.domain.entities.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends
        ListCrudRepository<ReservationEntity, Long>,
        PagingAndSortingRepository<ReservationEntity, Long> {
}
