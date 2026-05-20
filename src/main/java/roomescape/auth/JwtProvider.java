package roomescape.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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
import roomescape.auth.exception.ExpiredAccessTokenException;
import roomescape.auth.exception.InvalidAccessTokenException;

@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long expirationSeconds;
    private final Clock clock;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration-seconds}") Long expirationSeconds,
                       Clock clock) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
        this.clock = clock;
    }

    public String createAccessToken(Long memberId) {
        Instant now = Instant.now(clock);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(secretKey)
                .compact();
    }

    public Long extractSub(String accessToken) {
        try {
            String subjectStr = getClaims(accessToken)
                    .getSubject();

            return Long.parseLong(subjectStr);
        } catch (ExpiredJwtException e) {
            throw new ExpiredAccessTokenException();
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidAccessTokenException();
        }
    }

    private Claims getClaims(String accessToken) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();
    }

    public Long calculateExpiresIn(String accessToken) {
        try {
            Claims payload = getClaims(accessToken);

            Instant now = Instant.now(clock);
            Instant expiration = payload.getExpiration().toInstant();

            return Duration.between(now, expiration).getSeconds();
        } catch (ExpiredJwtException e) {
            throw new ExpiredAccessTokenException();
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidAccessTokenException();
        }
    }
}
