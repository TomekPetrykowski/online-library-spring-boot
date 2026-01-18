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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final Mapper<GenreEntity, GenreDto> genreMapper;

    @Override
    public GenreDto save(GenreDto genreDto) {
        GenreEntity genreEntity = genreMapper.mapFrom(genreDto);
        GenreEntity savedGenreEntity = genreRepository.save(genreEntity);
        return genreMapper.mapTo(savedGenreEntity);
    }

    @Override
    public List<GenreDto> findAll() {
        return StreamSupport.stream(genreRepository.findAll().spliterator(), false)
                .map(genreMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public Page<GenreDto> findAll(Pageable pageable) {
        Page<GenreEntity> foundGenres = genreRepository.findAll(pageable);
        return foundGenres.map(genreMapper::mapTo);
    }

    @Override
    public Optional<GenreDto> findById(Long id) {
        return genreRepository.findById(id).map(genreMapper::mapTo);
    }

    @Override
    public boolean isExists(Long id) {
        return genreRepository.existsById(id);
    }

    @Override
    public GenreDto partialUpdate(Long id, GenreDto genreDto) {
        genreDto.setId(id);

        return genreRepository.findById(id).map(existingGenre -> {
            Optional.ofNullable(genreDto.getName()).ifPresent(existingGenre::setName);
            return genreMapper.mapTo(genreRepository.save(existingGenre));
        }).orElseThrow(() -> new ResourceNotFoundException("Genre does not exist"));
    }

    @Override
    public void delete(Long id) {
        genreRepository.deleteById(id);
    }
}
