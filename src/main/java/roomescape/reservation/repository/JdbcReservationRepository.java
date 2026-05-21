package roomescape.reservation.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> {
        ReservationTime time = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("time_start_at").toLocalTime()
        );

        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail_url")
        );

        Member member = new Member(
                resultSet.getLong("member_id"),
                resultSet.getString("member_name"),
                resultSet.getString("member_email"),
                resultSet.getString("member_password_hash")
        );

        return new Reservation(
                resultSet.getLong("reservation_id"),
                member,
                resultSet.getDate("reservation_date").toLocalDate(),
                time,
                theme
        );
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Reservation save(Reservation reservation) {
        String sql = """
                INSERT INTO reservation (member_id, reservation_date, time_id, theme_id)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, reservation.getMember().getId());
            ps.setDate(2, Date.valueOf(reservation.getDate()));
            ps.setLong(3, reservation.getTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();

        return new Reservation(
                id,
                reservation.getMember(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
    }

    @Override
    public List<Reservation> findAllByMemberId(Long memberId) {
        String sql = """
                SELECT r.id AS reservation_id,
                       m.id AS member_id,
                       m.name AS member_name,
                       m.email AS member_email,
                       m.password_hash AS member_password_hash,
                       r.reservation_date,
                       t.id AS time_id,
                       t.start_at AS time_start_at,
                       h.id AS theme_id,
                       h.name AS theme_name,
                       h.description AS theme_description,
                       h.thumbnail_url AS theme_thumbnail_url
                FROM reservation r
                INNER JOIN reservation_time t
                  ON r.time_id = t.id
                INNER JOIN theme h
                  ON r.theme_id = h.id
                INNER JOIN member m
                  ON r.member_id = m.id
                WHERE m.id = ?
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, memberId);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT r.id AS reservation_id,
                       m.id AS member_id,
                       m.name AS member_name,
                       m.email AS member_email,
                       m.password_hash AS member_password_hash,
                       r.reservation_date,
                       t.id AS time_id,
                       t.start_at AS time_start_at,
                       h.id AS theme_id,
                       h.name AS theme_name,
                       h.description AS theme_description,
                       h.thumbnail_url AS theme_thumbnail_url
                FROM reservation r
                INNER JOIN reservation_time t
                  ON r.time_id = t.id
                INNER JOIN theme h
                  ON r.theme_id = h.id
                INNER JOIN member m
                  ON r.member_id = m.id
                WHERE r.id = ?
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, id)
                .stream().findFirst();
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE reservation_date = ? AND time_id = ? AND theme_id = ?
                )
                """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT r.id AS reservation_id,
                       m.id AS member_id,
                       m.name AS member_name,
                       m.email AS member_email,
                       m.password_hash AS member_password_hash,
                       r.reservation_date,
                       t.id AS time_id,
                       t.start_at AS time_start_at,
                       h.id AS theme_id,
                       h.name AS theme_name,
                       h.description AS theme_description,
                       h.thumbnail_url AS theme_thumbnail_url
                FROM reservation r
                INNER JOIN reservation_time t
                  ON r.time_id = t.id
                INNER JOIN theme h
                  ON r.theme_id = h.id
                INNER JOIN member m
                  ON r.member_id = m.id
                """;

        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public List<PopularThemeQueryResult> findPopularThemes(LocalDate from, LocalDate to, int limit) {
        String sql = """
                SELECT t.id,
                       t.name,
                       t.description,
                       t.thumbnail_url
                FROM reservation r
                INNER JOIN theme t
                  ON r.theme_id = t.id
                WHERE r.reservation_date >= ?
                  AND r.reservation_date <= ?
                GROUP BY t.id,
                         t.name,
                         t.description,
                         t.thumbnail_url
                ORDER BY COUNT(r.id) DESC,
                         t.id ASC
                LIMIT ?
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNum) -> new PopularThemeQueryResult(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("thumbnail_url")
                ),
                Date.valueOf(from),
                Date.valueOf(to),
                limit
        );
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeIdExceptId(LocalDate date, Long timeId, Long themeId, Long id) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE reservation_date = ? AND time_id = ? AND theme_id = ? AND id != ?
                )
                """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId, id);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void update(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET member_id = ?, reservation_date = ?, time_id = ?, theme_id = ?
                WHERE id = ?
                """;

        int affectedRow = jdbcTemplate.update(
                sql,
                reservation.getMember().getId(),
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getId()
        );

        if (affectedRow == 0) {
            throw new ReservationNotFoundException();
        }
    }

    @Override
    public int deleteById(Long id) {
        String sql = """
                DELETE FROM reservation
                WHERE id = ?
                """;

        return jdbcTemplate.update(sql, id);
    }
}
