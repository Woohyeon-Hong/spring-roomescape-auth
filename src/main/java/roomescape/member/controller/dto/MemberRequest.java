package roomescape.member.controller.dto;

import roomescape.global.validation.EmailFormatValidator;
import roomescape.member.exception.InvalidMemberRequestFormatException;
import roomescape.member.service.MemberCommand;

public record MemberRequest(
        String name,
        String email,
        String rawPassword
) {
    public MemberRequest {
        if (name == null || name.isBlank() ||
                rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidMemberRequestFormatException();
        }

        EmailFormatValidator.validate(email);
    }

    public MemberCommand toCommand() {
        return new MemberCommand(
                name, email, rawPassword
        );
    }
}
