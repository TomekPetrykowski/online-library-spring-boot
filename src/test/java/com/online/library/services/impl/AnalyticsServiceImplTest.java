package com.online.library.services.impl;

import com.online.library.domain.dao.AnalyticsDao;
import com.online.library.domain.dto.AuthorStatDto;
import com.online.library.domain.dto.BookStatDto;
import com.online.library.domain.dto.UserStatDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceImplTest {

    @Mock
    private AnalyticsDao analyticsDao;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    public void testGetMostPopularBooks() {
        BookStatDto bookStat = BookStatDto.builder()
                .id(1L)
                .title("Test Book")
                .averageRating(BigDecimal.valueOf(4.5))
                .reservationCount(10L)
                .build();

        when(analyticsDao.getMostPopularBooks(10)).thenReturn(List.of(bookStat));

        List<BookStatDto> result = analyticsService.getMostPopularBooks(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Book");
        verify(analyticsDao).getMostPopularBooks(10);
    }

    @Test
    public void testGetMostReadAuthors() {
        AuthorStatDto authorStat = AuthorStatDto.builder()
                .id(1L)
                .name("John")
                .lastName("Doe")
                .loanCount(5L)
                .build();

        when(analyticsDao.getMostReadAuthors(10)).thenReturn(List.of(authorStat));

        List<AuthorStatDto> result = analyticsService.getMostReadAuthors(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John");
        assertThat(result.get(0).getLastName()).isEqualTo("Doe");
        verify(analyticsDao).getMostReadAuthors(10);
    }

    @Test
    public void testGetMostActiveUsers() {
        UserStatDto userStat = UserStatDto.builder()
                .id(1L)
                .username("activeuser")
                .email("active@example.com")
                .reservationCount(15L)
                .build();

        when(analyticsDao.getMostActiveUsers(10)).thenReturn(List.of(userStat));

        List<UserStatDto> result = analyticsService.getMostActiveUsers(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("activeuser");
        verify(analyticsDao).getMostActiveUsers(10);
    }

    @Test
    public void testGetMostPopularBooksWithCustomLimit() {
        when(analyticsDao.getMostPopularBooks(5)).thenReturn(List.of());

        analyticsService.getMostPopularBooks(5);

        verify(analyticsDao).getMostPopularBooks(5);
    }

    @Test
    public void testGetMostReadAuthorsWithCustomLimit() {
        when(analyticsDao.getMostReadAuthors(20)).thenReturn(List.of());

        analyticsService.getMostReadAuthors(20);

        verify(analyticsDao).getMostReadAuthors(20);
    }

    @Test
    public void testGetMostActiveUsersWithCustomLimit() {
        when(analyticsDao.getMostActiveUsers(3)).thenReturn(List.of());

        analyticsService.getMostActiveUsers(3);

        verify(analyticsDao).getMostActiveUsers(3);
    }
}
