package com.online.library.repositories;

import com.online.library.domain.entities.RatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends
        ListCrudRepository<RatingEntity, Long>,
        PagingAndSortingRepository<RatingEntity, Long> {
}
