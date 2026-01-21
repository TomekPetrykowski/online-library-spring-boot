package com.online.library.services;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface BaseService<D, ID> {
    D save(D dto);

    Page<D> findAll(Pageable pageable);

    Optional<D> findById(ID id);

    boolean isExists(ID id);

    D partialUpdate(ID id, D dto);

    void delete(ID id);
}
