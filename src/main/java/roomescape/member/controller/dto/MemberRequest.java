package roomescape.member.controller.dto;

import java.util.regex.Pattern;
import roomescape.member.exception.InvalidEmailFormatException;
import roomescape.member.exception.InvalidMemberRequestFormatException;
import roomescape.member.service.MemberCommand;

public record MemberRequest(
        String name,
        String email,
        String rawPassword
) {
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public MemberRequest {
        if (name == null || name.isBlank() ||
        rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidMemberRequestFormatException();
        }

        if (email == null || email.isBlank() || !EMAIL.matcher(email).matches()) {
            throw new InvalidEmailFormatException();
        }
    }

    public MemberCommand toCommand() {
        return new MemberCommand(
                name, email, rawPassword
        );
    }
}
