package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.member.domain.Member;
import roomescape.member.exception.MemberNotFoundException;

@JdbcTest
class JdbcMemberRepositoryTest {

    MemberRepository memberRepository;

    @Autowired
    public JdbcMemberRepositoryTest(JdbcTemplate jdbcTemplate) {
        memberRepository = new JdbcMemberRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("새로운 회원을 가입하고 반환된 객체의 ID를 확인한다.")
    void saveTest_success() {
        // given
        Member member = Member.of("브라운", "example@gmail.com", "passwordHash");

        // when
        Member saved = memberRepository.save(member);

        //then
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("email이 기존에 이미 등록돼 있으면 예외가 발생한다.")
    void saveTest_duplicate_email() {
        // given
        memberRepository.save(Member.of("브라운", "example@gmail.com", "passwordHash"));

        // when & then
        assertThatThrownBy(
                () -> memberRepository.save(Member.of("otherName", "example@gmail.com", "passwordHashPOther")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("email을 기준으로 회원을 조회한다.")
    @Test
    void findByEmailTest() {
        //given
        memberRepository.save(Member.of("브라운", "example@gmail.com", "passwordHash"));

        //when
        Member foud = memberRepository.getByEmail("example@gmail.com");

        //when & then
        assertThat(foud.getName()).isEqualTo("브라운");
        assertThat(foud.getEmail()).isEqualTo("example@gmail.com");
        assertThat(foud.getPasswordHash()).isEqualTo("passwordHash");
    }

    @DisplayName("email을 기준으로 회원 가입 여부를 조회한다.")
    @Test
    void existByEmailTest() {
        //given
        memberRepository.save(Member.of("브라운", "example@gmail.com", "passwordHash"));

        //when & then
        assertThat(memberRepository.existByEmail("example@gmail.com")).isTrue();
        assertThat(memberRepository.existByEmail("other@gmail.com")).isFalse();
    }

    @DisplayName("email을 기준으로 회원을 조회한다.")
    @Test
    void getByIdTest() {
        //given
        Member saved = memberRepository.save(Member.of("브라운", "example@gmail.com", "passwordHash"));

        //when & then
        assertThat(memberRepository.getById(saved.getId())).isNotNull();
        assertThatThrownBy(() -> memberRepository.getById(999L))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("id를 기준으로 회원을 삭제한다.")
    void deleteByIdTest() {
        // given
        Member saved = memberRepository.save(Member.of("브라운", "example@gmail.com", "passwordHash"));

        // when
        int deletedCount = memberRepository.deleteById(saved.getId());

        // then
        assertThat(deletedCount).isEqualTo(1);
    }

    @DisplayName("id를 기준으로 회원 가입 여부를 조회한다.")
    @Test
    void existByIdTest() {
        //given
        Member saved = memberRepository.save(Member.of("브라운", "example@gmail.com", "passwordHash"));

        //when & then
        assertThat(memberRepository.existById(saved.getId())).isTrue();
        assertThat(memberRepository.existById(999L)).isFalse();
    }
}
