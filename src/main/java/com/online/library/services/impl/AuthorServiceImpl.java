package com.online.library.services.impl;

import com.online.library.domain.dto.AuthorDto;
import com.online.library.domain.entities.AuthorEntity;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.AuthorRepository;
import com.online.library.services.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final Mapper<AuthorEntity, AuthorDto> authorMapper;

    @Override
    public AuthorDto save(AuthorDto authorDto) {
        AuthorEntity authorEntity = authorMapper.mapFrom(authorDto);
        AuthorEntity savedAuthorEntity = authorRepository.save(authorEntity);
        return authorMapper.mapTo(savedAuthorEntity);
    }

    @Override
    public List<AuthorDto> findAll() {
        return StreamSupport.stream(authorRepository.findAll().spliterator(), false)
                .map(authorMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AuthorDto> findById(Long id) {
        return authorRepository.findById(id).map(authorMapper::mapTo);
    }

    @Override
    public boolean isExists(Long id) {
        return authorRepository.existsById(id);
    }

    @Override
    public AuthorDto partialUpdate(Long id, AuthorDto authorDto) {
        authorDto.setId(id);

        return authorRepository.findById(id).map(existingAuthor -> {
            Optional.ofNullable(authorDto.getName()).ifPresent(existingAuthor::setName);
            Optional.ofNullable(authorDto.getLastName()).ifPresent(existingAuthor::setLastName);
            Optional.ofNullable(authorDto.getBio()).ifPresent(existingAuthor::setBio);
            return authorMapper.mapTo(authorRepository.save(existingAuthor));
        }).orElseThrow(() -> new ResourceNotFoundException("Author does not exist"));
    }

    @Override
    public void delete(Long id) {
        authorRepository.deleteById(id);
    }
}
