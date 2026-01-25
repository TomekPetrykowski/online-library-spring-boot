package com.online.library.services;

import com.online.library.domain.dto.AuthorStatDto;
import com.online.library.domain.dto.BookStatDto;
import com.online.library.domain.dto.UserStatDto;

import java.util.List;

public interface ExportService {

    byte[] exportPopularBooksToCsv(List<BookStatDto> books);

    byte[] exportReadAuthorsToCsv(List<AuthorStatDto> authors);

    byte[] exportActiveUsersToCsv(List<UserStatDto> users);
}
