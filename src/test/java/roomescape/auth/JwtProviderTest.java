package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.auth.exception.ExpiredAccessTokenException;

class JwtProviderTest {

    private static final String TEST_SECRET = "test_8mV9rQ2xKp7Ld4NzTt1YwE6uJc3Hs5BxQf0Ra8DnUv4";

    @DisplayName("memberId로 토큰을 생성할 수 있다.")
    @Test
    void createAccessTokenTest() {
        //given
        JwtProvider jwtProvider = new JwtProvider(
                TEST_SECRET,
                3600L,
                Clock.fixed(
                        Instant.parse("2026-05-01T00:00:00Z"),
                        ZoneId.of("Asia/Seoul")
                )
        );

        //when & then
        assertThatCode(() -> jwtProvider.createAccessToken(1L))
                .doesNotThrowAnyException();
    }

    @DisplayName("토큰에서 memberId를 추출한다.")
    @Test
    void extractSubTest_success() {
        //given
        JwtProvider jwtProvider = new JwtProvider(
                TEST_SECRET,
                3600L,
                Clock.fixed(
                        Instant.parse("2026-05-01T00:00:00Z"),
                        ZoneId.of("Asia/Seoul")
                )
        );

        String accessToken = jwtProvider.createAccessToken(1L);

        //when & then
        assertThat(jwtProvider.extractSub(accessToken))
                .isEqualTo(1L);
    }

    @DisplayName("sub 추출 시, 토큰이 만료됐으면 예외가 발생한다.")
    @Test
    void extractSubTest_expire() {
        //given
        Clock before = Clock.fixed(
                Instant.parse("2026-05-01T00:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        JwtProvider beforeProvider = new JwtProvider(
                TEST_SECRET,
                1L,
                before
        );

        String accessToken = beforeProvider.createAccessToken(1L);

        Clock after = Clock.offset(before, Duration.ofSeconds(2));

        JwtProvider afterProvider = new JwtProvider(
                TEST_SECRET,
                1L,
                after);

        //when & then
        assertThatThrownBy(() -> afterProvider.extractSub(accessToken))
                .isInstanceOf(ExpiredAccessTokenException.class);
    }

    @DisplayName("토큰의 만료시간까지 남은 초를 반환한다.")
    @Test
    void calculateExpiresInTest() {
        //given
        Clock before = Clock.fixed(
                Instant.parse("2026-05-01T00:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        JwtProvider beforeProvider = new JwtProvider(
                TEST_SECRET,
                3L,
                before
        );

        String accessToken = beforeProvider.createAccessToken(1L);

        Clock after = Clock.offset(before, Duration.ofSeconds(1));

        JwtProvider afterProvider = new JwtProvider(
                TEST_SECRET,
                1L,
                after);

        //when & then
        assertThat(afterProvider.calculateExpiresIn(accessToken))
                .isEqualTo(2L);
    }
}
