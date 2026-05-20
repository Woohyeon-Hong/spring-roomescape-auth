package roomescape.auth.exception;

import roomescape.global.exception.InvalidRequestFormatException;

public class InvalidLoginRequestFormatException extends InvalidRequestFormatException {

    public InvalidLoginRequestFormatException() {
        super("잘못된 로그인 요청 형식입니다.");
    }
}
