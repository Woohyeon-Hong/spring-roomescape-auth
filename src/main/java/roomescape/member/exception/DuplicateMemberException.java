package roomescape.member.exception;

import roomescape.global.exception.DuplicateException;

public class DuplicateMemberException extends DuplicateException {

    public DuplicateMemberException() {
        super("이미 회원이 가입돼 있습니다.");
    }
}
