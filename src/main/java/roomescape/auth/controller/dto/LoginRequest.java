package roomescape.auth.controller.dto;

import roomescape.auth.exception.InvalidLoginRequestFormatException;
import roomescape.auth.service.dto.LoginCommand;
import roomescape.global.validation.EmailFormatValidator;

public record LoginRequest(
        String email,
        String password
) {

    public LoginRequest {
        if (password == null || password.isBlank()) {
            throw new InvalidLoginRequestFormatException();
        }

        EmailFormatValidator.validate(email);
    }

    public LoginCommand toCommand() {
        return new LoginCommand(
                this.email,
                this.password
        );
    }
}
