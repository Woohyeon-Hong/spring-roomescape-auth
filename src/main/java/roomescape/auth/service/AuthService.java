package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.JwtProvider;
import roomescape.auth.exception.LoginFailureException;
import roomescape.auth.service.dto.LoginCommand;
import roomescape.auth.service.dto.LoginResult;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;

@Service
public class AuthService {

    private static final String BEARER = "BEARER";

    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    public AuthService(MemberService memberService, JwtProvider jwtProvider) {
        this.memberService = memberService;
        this.jwtProvider = jwtProvider;
    }

    public LoginResult login(LoginCommand command) {
        Member member = memberService.getByEmail(command.email());

        if (!member.checkPasswordWith(command.password())) {
            throw new LoginFailureException();
        }

        String token = jwtProvider.createAccessToken(member.id());
        Long expiresIn = jwtProvider.calculateExpiresIn(token);

        return new LoginResult(
                token,
                BEARER,
                expiresIn
        );
    }
}
