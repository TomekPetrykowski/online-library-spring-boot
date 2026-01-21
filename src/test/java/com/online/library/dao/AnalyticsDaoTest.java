package com.online.library.dao;

import com.online.library.domain.dao.AnalyticsDao;
import com.online.library.domain.dto.AuthorStatDto;
import com.online.library.domain.dto.BookStatDto;
import com.online.library.domain.dto.UserStatDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class AnalyticsDaoTest {

    @Autowired
    private AnalyticsDao analyticsDao;

    @Test
    public void testGetMostPopularBooksReturnsResults() {
        List<BookStatDto> result = analyticsDao.getMostPopularBooks(10);
        assertThat(result).isNotNull();
    }

    @Test
    public void testGetMostPopularBooksWithLimit() {
        List<BookStatDto> result = analyticsDao.getMostPopularBooks(5);
        assertThat(result).isNotNull();
        assertThat(result.size()).isLessThanOrEqualTo(5);
    }

    @Test
    public void testGetMostReadAuthorsReturnsResults() {
        List<AuthorStatDto> result = analyticsDao.getMostReadAuthors(10);
        assertThat(result).isNotNull();
    }

    @Test
    public void testGetMostReadAuthorsWithLimit() {
        List<AuthorStatDto> result = analyticsDao.getMostReadAuthors(5);
        assertThat(result).isNotNull();
        assertThat(result.size()).isLessThanOrEqualTo(5);
    }

    @Test
    public void testGetMostActiveUsersReturnsResults() {
        List<UserStatDto> result = analyticsDao.getMostActiveUsers(10);
        assertThat(result).isNotNull();
    }

    @Test
    public void testGetMostActiveUsersWithLimit() {
        List<UserStatDto> result = analyticsDao.getMostActiveUsers(5);
        assertThat(result).isNotNull();
        assertThat(result.size()).isLessThanOrEqualTo(5);
    }

    @Test
    public void testBookStatDtoContainsExpectedFields() {
        List<BookStatDto> result = analyticsDao.getMostPopularBooks(1);
        if (!result.isEmpty()) {
            BookStatDto stat = result.get(0);
            assertThat(stat.getId()).isNotNull();
            assertThat(stat.getTitle()).isNotNull();
        }
    }

    @Test
    public void testAuthorStatDtoContainsExpectedFields() {
        List<AuthorStatDto> result = analyticsDao.getMostReadAuthors(1);
        if (!result.isEmpty()) {
            AuthorStatDto stat = result.get(0);
            assertThat(stat.getId()).isNotNull();
            assertThat(stat.getName()).isNotNull();
            assertThat(stat.getLastName()).isNotNull();
        }
    }

    @Test
    public void testUserStatDtoContainsExpectedFields() {
        List<UserStatDto> result = analyticsDao.getMostActiveUsers(1);
        if (!result.isEmpty()) {
            UserStatDto stat = result.get(0);
            assertThat(stat.getId()).isNotNull();
            assertThat(stat.getUsername()).isNotNull();
        }
    }
}
