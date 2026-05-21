package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.domain.Member;
import roomescape.member.exception.DuplicateMemberException;
import roomescape.member.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @DisplayName("회원 가입 시, 비밀번호를 암호화해서 저장한다.")
    @Test
    void signUpTest_success() {
        // given
        MemberCommand command = new MemberCommand("홍길동", "test@test.com", "rawPassword");

        when(memberRepository.existByEmail(command.email()))
                .thenReturn(false);

        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Member saved = memberService.signUp(command);

        // then
        assertThat(saved.getPasswordHash()).isNotEqualTo("rawPassword");
        assertThat(BCrypt.checkpw("rawPassword", saved.getPasswordHash())).isTrue();
    }

    @DisplayName("회원 가입 시, 기존에 이미 동일한 이메일로 회원가입이 돼 있으면 예외가 발생한다.")
    @Test
    void signUpTest_duplicate() {
        //given
        when(memberRepository.existByEmail("example@gmail.com"))
                .thenReturn(true);

        //when & then
        assertThatThrownBy(() -> memberService.signUp(
                new MemberCommand("브라운", "example@gmail.com", "rawPassword")
        )).isInstanceOf(DuplicateMemberException.class);
    }

    @DisplayName("email을 기반으로 회원을 조회한다.")
    @Test
    void getByEmailTest() {
        //given
        when(memberRepository.findByEmail("example@gmail.com"))
                .thenReturn(Optional.of(new Member(1L, "브라운", "example@gmail.com", "passwordHash")));

        //when & then
        assertThat(memberService.getByEmail("example@gmail.com").getEmail())
                .isEqualTo("example@gmail.com");
    }
}
