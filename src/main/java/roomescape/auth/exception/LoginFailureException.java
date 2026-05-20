package roomescape.auth.exception;

import roomescape.global.exception.BusinessException;

public class LoginFailureException extends BusinessException {

    public LoginFailureException() {
        super("이메일 또는 비밀번호가 유효하지 않습니다.");
    }
}
