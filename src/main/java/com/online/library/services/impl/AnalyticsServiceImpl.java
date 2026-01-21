package com.online.library.services.impl;

import com.online.library.domain.dao.AnalyticsDao;
import com.online.library.domain.dto.AuthorStatDto;
import com.online.library.domain.dto.BookStatDto;
import com.online.library.domain.dto.UserStatDto;
import com.online.library.services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsDao analyticsDao;

    @Override
    public List<BookStatDto> getMostPopularBooks(int limit) {
        log.info("Getting {} most popular books", limit);
        return analyticsDao.getMostPopularBooks(limit);
    }

    @Override
    public List<AuthorStatDto> getMostReadAuthors(int limit) {
        log.info("Getting {} most read authors", limit);
        return analyticsDao.getMostReadAuthors(limit);
    }

    @Override
    public List<UserStatDto> getMostActiveUsers(int limit) {
        log.info("Getting {} most active users", limit);
        return analyticsDao.getMostActiveUsers(limit);
    }
}
