package com.online.library.services;

import java.util.List;
import java.util.Optional;

public interface BaseService<D, ID> {
    D save(D dto);
    List<D> findAll();
    Optional<D> findById(ID id);
    boolean isExists(ID id);
    D partialUpdate(ID id, D dto);
    void delete(ID id);
}
