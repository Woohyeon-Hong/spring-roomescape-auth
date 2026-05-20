package roomescape.member.service;

public record MemberCommand(
        String name,
        String email,
        String rawPassword
) {}
