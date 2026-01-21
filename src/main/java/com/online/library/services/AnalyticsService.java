package com.online.library.services;

import com.online.library.domain.dto.AuthorStatDto;
import com.online.library.domain.dto.BookStatDto;
import com.online.library.domain.dto.UserStatDto;

import java.util.List;

public interface AnalyticsService {
    List<BookStatDto> getMostPopularBooks(int limit);

    List<AuthorStatDto> getMostReadAuthors(int limit);

    List<UserStatDto> getMostActiveUsers(int limit);
}
