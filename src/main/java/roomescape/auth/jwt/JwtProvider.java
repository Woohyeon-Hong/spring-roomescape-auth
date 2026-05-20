package roomescape.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final Long expirationSeconds;
    private final Clock clock;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration-seconds}") Long expirationSeconds,
                       Clock clock) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
        this.clock = clock;
    }

    public String createToken(Long memberId) {
        Instant now = Instant.now(clock);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(secretKey)
                .compact();
    }

    public Long extractSub(String token) {
        String subjectStr = Jwts.parser().verifyWith(secretKey)
                .clock(jwtClock())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        return Long.parseLong(subjectStr);
    }

    private io.jsonwebtoken.Clock jwtClock() {
        return new io.jsonwebtoken.Clock() {
            @Override
            public Date now() {
                return Date.from(clock.instant());
            }
        };
    }

    public Long calculateExpiresIn(String token) {
        Claims payload = Jwts.parser().verifyWith(secretKey)
                .clock(jwtClock())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Instant now = Instant.now(clock);
        Instant expiration = payload.getExpiration().toInstant();

        return Math.max(0, Duration.between(now, expiration).getSeconds());
    }

    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey)
                .clock(jwtClock())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }
}
