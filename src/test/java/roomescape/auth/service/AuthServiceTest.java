package roomescape.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.JwtProvider;
import roomescape.auth.exception.LoginFailureException;
import roomescape.auth.service.dto.LoginCommand;
import roomescape.auth.service.dto.LoginResult;
import roomescape.member.domain.Member;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.member.service.MemberService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    MemberService memberService;

    @Mock
    JwtProvider jwtProvider;

    @InjectMocks
    AuthService authService;

    @DisplayName("이메일과 비밀번호가 일치하면 로그인에 성공하고, access Token을 반환한다.")
    @Test
    void loginTest_success() {
        //given
        String rawPassword = "rawPassword";
        String passwordHash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        when(memberService.getByEmail("target@gmail.com"))
                .thenReturn(new Member(1L, "브라운", "target@gmail.com", passwordHash));

        when(jwtProvider.createAccessToken(1L))
                .thenReturn("mock-access-token");

        when(jwtProvider.calculateExpiresIn("mock-access-token"))
                .thenReturn(3600L);

        //when
        LoginResult loginResult =
                authService.login(new LoginCommand("target@gmail.com", "rawPassword"));

        //then
        assertThat(loginResult.accessToken()).isEqualTo("mock-access-token");
    }

    @DisplayName("회원가입이 돼 있지 않으면 로그인에 실패한다.")
    @Test
    void loginTest_not_found() {
        //given
        when(memberService.getByEmail("target@gmail.com"))
                .thenThrow(new MemberNotFoundException());

        //when & then
        assertThatThrownBy(
                () -> authService.login(new LoginCommand("target@gmail.com", "rawPassword"))
        ).isInstanceOf(MemberNotFoundException.class);
    }

    @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다.")
    @Test
    void loginTest_different_password() {
        //given
        String rawPassword = "rawPassword";
        String passwordHash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        when(memberService.getByEmail("target@gmail.com"))
                .thenReturn(new Member(1L, "브라운", "target@gmail.com", passwordHash));

        //when & then
        assertThatThrownBy(
                () -> authService.login(new LoginCommand("target@gmail.com", "otherPassword"))
        ).isInstanceOf(LoginFailureException.class);
    }
}
