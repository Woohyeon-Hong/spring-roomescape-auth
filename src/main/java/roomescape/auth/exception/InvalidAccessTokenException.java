package roomescape.auth.exception;

public class InvalidAccessTokenException extends AuthenticationException {

    public InvalidAccessTokenException() {
        super("토큰이 유효하지 않습니다.");
    }
}
