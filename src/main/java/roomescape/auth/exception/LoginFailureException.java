package roomescape.auth.exception;

public class LoginFailureException extends AuthenticationException {

    public LoginFailureException() {
        super("이메일 또는 비밀번호가 유효하지 않습니다.");
    }
}
