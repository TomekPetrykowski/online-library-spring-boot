package com.online.library.domain.dao;

import com.online.library.domain.dto.AuthorStatDto;
import com.online.library.domain.dto.BookStatDto;
import com.online.library.domain.dto.UserStatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AnalyticsDao {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<BookStatDto> BOOK_STAT_ROW_MAPPER = (rs, rowNum) -> BookStatDto.builder()
            .id(rs.getLong("id"))
            .title(rs.getString("title"))
            .averageRating(rs.getBigDecimal("average_rating"))
            .reservationCount(rs.getLong("reservation_count"))
            .build();

    private static final RowMapper<AuthorStatDto> AUTHOR_STAT_ROW_MAPPER = (rs, rowNum) -> AuthorStatDto.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .lastName(rs.getString("last_name"))
            .loanCount(rs.getLong("loan_count"))
            .build();

    private static final RowMapper<UserStatDto> USER_STAT_ROW_MAPPER = (rs, rowNum) -> UserStatDto.builder()
            .id(rs.getLong("id"))
            .username(rs.getString("username"))
            .email(rs.getString("email"))
            .reservationCount(rs.getLong("reservation_count"))
            .build();

    public List<BookStatDto> getMostPopularBooks(int limit) {
        log.debug("Fetching {} most popular books", limit);
        String sql = """
                SELECT b.id, b.title, b.average_rating, COUNT(r.id) as reservation_count
                FROM books b
                LEFT JOIN reservations r ON b.id = r.book_id
                GROUP BY b.id, b.title, b.average_rating
                ORDER BY b.average_rating DESC NULLS LAST
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, BOOK_STAT_ROW_MAPPER, limit);
    }

    public List<AuthorStatDto> getMostReadAuthors(int limit) {
        log.debug("Fetching {} most read authors", limit);
        String sql = """
                SELECT a.id, a.name, a.last_name, COUNT(r.id) as loan_count
                FROM authors a
                JOIN book_authors ba ON a.id = ba.author_id
                JOIN books b ON ba.book_id = b.id
                LEFT JOIN reservations r ON b.id = r.book_id AND r.status IN ('WYPOŻYCZONA', 'ZWRÓCONA')
                GROUP BY a.id, a.name, a.last_name
                ORDER BY loan_count DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, AUTHOR_STAT_ROW_MAPPER, limit);
    }

    public List<UserStatDto> getMostActiveUsers(int limit) {
        log.debug("Fetching {} most active users", limit);
        String sql = """
                SELECT u.id, u.username, u.email, COUNT(r.id) as reservation_count
                FROM users u
                LEFT JOIN reservations r ON u.id = r.user_id
                GROUP BY u.id, u.username, u.email
                ORDER BY reservation_count DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, USER_STAT_ROW_MAPPER, limit);
    }

//    public int updateBookAverageRating(Long bookId, java.math.BigDecimal averageRating) {
//        log.debug("Updating average rating for book id={} to {}", bookId, averageRating);
//        String sql = "UPDATE books SET average_rating = ? WHERE id = ?";
//        return jdbcTemplate.update(sql, averageRating, bookId);
//    }
//
//    public int deleteOldReturnedReservations(int daysOld) {
//        log.debug("Deleting returned reservations older than {} days", daysOld);
//        String sql = """
//                DELETE FROM reservations
//                WHERE status = 'ZWRÓCONA'
//                AND returned_at < CURRENT_DATE - INTERVAL '%d days'
//                """.formatted(daysOld);
//        return jdbcTemplate.update(sql);
//    }
}
