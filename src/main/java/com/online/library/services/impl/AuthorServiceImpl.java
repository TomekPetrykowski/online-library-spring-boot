package com.online.library.services.impl;

import com.online.library.domain.dto.AuthorDto;
import com.online.library.domain.entities.AuthorEntity;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.AuthorRepository;
import com.online.library.services.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final Mapper<AuthorEntity, AuthorDto> authorMapper;

    @Override
    @Transactional
    public AuthorDto save(AuthorDto authorDto) {
        AuthorEntity authorEntity = authorMapper.mapFrom(authorDto);
        AuthorEntity savedAuthorEntity = authorRepository.save(authorEntity);
        return authorMapper.mapTo(savedAuthorEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuthorDto> findAll(Pageable pageable) {
        Page<AuthorEntity> foundAuthors = authorRepository.findAll(pageable);
        return foundAuthors.map(authorMapper::mapTo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthorDto> findById(Long id) {
        return authorRepository.findById(id).map(authorMapper::mapTo);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExists(Long id) {
        return authorRepository.existsById(id);
    }

    @Override
    @Transactional
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
    @Transactional
    public void delete(Long id) {
        authorRepository.deleteById(id);
    }
}
