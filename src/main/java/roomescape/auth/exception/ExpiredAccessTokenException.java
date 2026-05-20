package roomescape.auth.exception;

public class ExpiredAccessTokenException extends AuthenticationException {

    public ExpiredAccessTokenException() {
        super("토큰이 만료됐습니다.");
    }
}
