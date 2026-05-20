package roomescape.auth.exception;

import roomescape.global.exception.BusinessException;

public class AuthenticationException extends BusinessException {

    public AuthenticationException(String message) {
        super(message);
    }
}
