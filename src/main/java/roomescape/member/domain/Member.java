package roomescape.member.domain;

import java.util.Objects;
import org.mindrot.jbcrypt.BCrypt;

public class Member {

    private final Long id;
    private final String name;
    private final String email;
    private final String passwordHash;

    public Member(Long id, String name, String email, String passwordHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public static Member of(String name, String email, String passwordHash) {
        return new Member(null, name, email, passwordHash);
    }

    public Member updateId(Long id) {
        return new Member(
                id,
                this.getName(),
                this.getEmail(),
                this.getPasswordHash()
        );
    }

    public boolean checkPasswordWith(String password) {
        return BCrypt.checkpw(password, this.passwordHash);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Member member)) {
            return false;
        }
        return Objects.equals(getId(), member.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
