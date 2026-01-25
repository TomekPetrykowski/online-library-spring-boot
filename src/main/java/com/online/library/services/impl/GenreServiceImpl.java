package com.online.library.services.impl;

import com.online.library.domain.dto.GenreDto;
import com.online.library.domain.entities.GenreEntity;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.GenreRepository;
import com.online.library.services.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final Mapper<GenreEntity, GenreDto> genreMapper;

    @Override
    @Transactional
    public GenreDto save(GenreDto genreDto) {
        GenreEntity genreEntity = genreMapper.mapFrom(genreDto);
        GenreEntity savedGenreEntity = genreRepository.save(genreEntity);
        return genreMapper.mapTo(savedGenreEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GenreDto> findAll(Pageable pageable) {
        Page<GenreEntity> foundGenres = genreRepository.findAll(pageable);
        return foundGenres.map(genreMapper::mapTo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GenreDto> findById(Long id) {
        return genreRepository.findById(id).map(genreMapper::mapTo);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExists(Long id) {
        return genreRepository.existsById(id);
    }

    @Override
    @Transactional
    public GenreDto partialUpdate(Long id, GenreDto genreDto) {
        genreDto.setId(id);

        return genreRepository.findById(id).map(existingGenre -> {
            Optional.ofNullable(genreDto.getName()).ifPresent(existingGenre::setName);
            return genreMapper.mapTo(genreRepository.save(existingGenre));
        }).orElseThrow(() -> new ResourceNotFoundException("Genre does not exist"));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        genreRepository.deleteById(id);
    }
}
