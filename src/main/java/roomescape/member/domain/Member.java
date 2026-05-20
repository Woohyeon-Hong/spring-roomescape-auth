package roomescape.member.domain;

import java.util.Objects;
import org.mindrot.jbcrypt.BCrypt;
import roomescape.member.exception.InvalidMemberRequestFormatException;

public record Member(Long id, String name, String email, String passwordHash) {

    public Member {
        validate(name, email, passwordHash);

    }

    public static Member of(String name, String email, String passwordHash) {
        return new Member(null, name, email, passwordHash);
    }

    public Member updateId(Long id) {
        return new Member(
                id,
                this.name(),
                this.email(),
                this.passwordHash()
        );
    }

    private void validate(String name, String email, String password) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            throw new InvalidMemberRequestFormatException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Member member)) {
            return false;
        }
        return Objects.equals(id(), member.id());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id());
    }

    public boolean checkPasswordWith(String password) {
        return BCrypt.checkpw(password, this.passwordHash);
    }
}
