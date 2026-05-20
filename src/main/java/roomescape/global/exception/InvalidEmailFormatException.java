package roomescape.global.exception;

public class InvalidEmailFormatException extends InvalidRequestFormatException {

    public InvalidEmailFormatException() {
        super("이메일 형식이 유효하지 않습니다.");
    }
}
