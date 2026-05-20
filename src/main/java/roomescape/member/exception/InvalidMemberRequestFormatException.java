package roomescape.member.exception;

import roomescape.global.exception.InvalidRequestFormatException;

public class InvalidMemberRequestFormatException extends InvalidRequestFormatException {

    public InvalidMemberRequestFormatException() {
        super("회원 요청 형식이 유효하지 않습니다.");
    }
}
