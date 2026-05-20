package roomescape.auth.service.dto;

public record LoginCommand(
        String email,
        String password
) {}
