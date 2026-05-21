package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.domain.Member;
import roomescape.member.exception.DuplicateMemberException;
import roomescape.member.exception.MemberNotFoundException;
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
        MemberCommand command = new MemberCommand("brown", "brown@gmail.com", "rawPassword");

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
        when(memberRepository.existByEmail("brown@gmail.com"))
                .thenReturn(true);

        //when & then
        assertThatThrownBy(() -> memberService.signUp(
                new MemberCommand("브라운", "brown@gmail.com", "rawPassword")
        )).isInstanceOf(DuplicateMemberException.class);
    }

    @DisplayName("email을 기반으로 회원을 조회한다.")
    @Test
    void getByEmailTest() {
        //given
        when(memberRepository.getByEmail("brown@gmail.com"))
                .thenReturn(new Member(1L, "브라운", "brown@gmail.com", "passwordHash"));

        //when & then
        assertThat(memberService.getByEmail("brown@gmail.com").getEmail())
                .isEqualTo("brown@gmail.com");
    }

    @DisplayName("회원을 삭제한다.")
    @Test
    void signOutTest_success() {
        //given
        when(memberRepository.existById(anyLong()))
                .thenReturn(true);

        when(memberRepository.deleteById(anyLong()))
                .thenReturn(1);

        //when & then
        assertThatCode(() -> memberService.signOut(1L))
                .doesNotThrowAnyException();
    }

    @DisplayName("회원 삭제 시, 회원 가입이 안 돼 있으면 예외가 발생한다.")
    @Test
    void signOutTest_not_found() {
        //given
        when(memberRepository.existById(anyLong()))
                .thenReturn(false);
        //when & then
        assertThatThrownBy(() -> memberService.signOut(1L))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @DisplayName("회원 삭제 시, 동시에 삭제가 이루어지면 예외가 발생한다.")
    @Test
    void signOutTest_concurrent() {
        //given
        when(memberRepository.existById(anyLong()))
                .thenReturn(true);

        when(memberRepository.deleteById(anyLong()))
                .thenReturn(0);

        //when & then
        assertThatThrownBy(() -> memberService.signOut(1L))
                .isInstanceOf(MemberNotFoundException.class);
    }
}
