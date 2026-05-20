package roomescape.auth.controller.dto;

import roomescape.auth.service.dto.LoginResult;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
    public static LoginResponse from(LoginResult loginResult) {
        return new LoginResponse(
                loginResult.accessToken(),
                loginResult.tokenType(),
                loginResult.expiresIn()
        );
    }
}
