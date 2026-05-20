package roomescape.member.repository;

import java.sql.PreparedStatement;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;

@Repository
public class JdbcMemberRepository implements MemberRepository{

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
    public Optional<Member> findByEmail(String email) {
        String sql = """
               SELECT id, name, email, password_hash
               FROM member
               WHERE email = ?
               """;

        return jdbcTemplate.query(
                sql,
                MEMBER_ROW_MAPPER,
                email
        ).stream().findFirst();
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
    public int deleteByEmail(String email) {
        String sql = """
               DELETE FROM member
               WHERE email = ?
               """;

        return jdbcTemplate.update(sql, email);
    }
}
