package roomescape.member.repository;

import java.sql.PreparedStatement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.exception.MemberNotFoundException;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private static final RowMapper<Member> MEMBER_ROW_MAPPER = (resultSet, rowNum) ->
            new Member(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("email"),
                    resultSet.getString("password_hash")
            );


    private final JdbcTemplate jdbcTemplate;

    public JdbcMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Member save(Member member) {
        String sql = """
                INSERT INTO member (name, email, password_hash)
                VALUES (?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPasswordHash());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();

        return member.updateId(id);
    }

    @Override
    public Member getByEmail(String email) {
        String sql = """
                SELECT id, name, email, password_hash
                FROM member
                WHERE email = ?
                """;

        return jdbcTemplate.query(
                        sql,
                        MEMBER_ROW_MAPPER,
                        email
                ).stream().findFirst()
                .orElseThrow(MemberNotFoundException::new);
    }

    @Override
    public boolean existByEmail(String email) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM member
                    WHERE email = ?
                )
                """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, email);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public Member getById(Long id) {
        String sql = """
                SELECT id, name, email, password_hash
                FROM member
                WHERE id = ?
                """;

        return jdbcTemplate.query(
                        sql,
                        MEMBER_ROW_MAPPER,
                        id
                ).stream().findFirst()
                .orElseThrow(MemberNotFoundException::new);
    }

    @Override
    public boolean existById(Long id) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM member
                    WHERE id = ?
                )
                """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, id);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public int deleteById(Long id) {
        String sql = """
                DELETE FROM member
                WHERE id = ?
                """;

        return jdbcTemplate.update(sql, id);
    }
}
