package roomescape.auth.service.dto;

public record LoginResult(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
