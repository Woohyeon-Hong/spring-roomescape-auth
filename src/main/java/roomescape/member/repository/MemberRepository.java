package roomescape.member.repository;

import roomescape.member.domain.Member;

public interface MemberRepository {

    Member save(Member member);

    Member getByEmail(String email);

    boolean existByEmail(String email);

    Member getById(Long id);

    boolean existById(Long id);

    int deleteById(Long id);
}
