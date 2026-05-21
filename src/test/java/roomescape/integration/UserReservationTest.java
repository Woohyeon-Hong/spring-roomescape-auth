package roomescape.integration;


import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.integration.support.DatabaseHelper;
import roomescape.integration.support.SpringWebTest;
import roomescape.time.domain.ReservationTime;

@SpringWebTest
public class UserReservationTest {

    @Autowired
    DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @Test
    void 예약_가능한_시간_목록_조회() {
        createReservationTime("10:00");
        createReservationTime("11:00");
        createReservationTime("12:00");
        createReservationTime("13:00");

        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/image.png");
        createTheme("페어 테마", "페어 전용 테마입니다.", "https://example.com/pair.png");

        createMember("브라운", "example@gmail.com", "rawPassword");

        List<ReservationTime> beforeReservationResults = getAvailableTimes(LocalDate.of(2026, 5, 5), 1L);

        assertThat(beforeReservationResults).hasSize(4);
        assertThat(beforeReservationResults.stream().map(ReservationTime::getId).toList())
                .containsExactly(1L, 2L, 3L, 4L);
        assertThat(beforeReservationResults.stream().map(ReservationTime::getStartAt).toList())
                .containsExactly(
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        LocalTime.of(12, 0),
                        LocalTime.of(13, 0)
                );

        createReservation("example@gmail.com", "rawPassword",
                1L, LocalDate.of(2026, 5, 5), 1L, 1L);
        createReservation("example@gmail.com", "rawPassword",
                1L, LocalDate.of(2026, 5, 6), 2L, 2L);

        assertThat(getAvailableTimes(LocalDate.of(2026, 5, 5), 1L)).hasSize(3);
        assertThat(getAvailableTimes(LocalDate.of(2026, 5, 6), 1L)).hasSize(4);
        assertThat(getAvailableTimes(LocalDate.of(2026, 5, 5), 2L)).hasSize(4);
        assertThat(getAvailableTimes(LocalDate.of(2026, 5, 6), 2L)).hasSize(3);
    }

    private void createReservationTime(String startAt) {
        Map<String, Object> body = new HashMap<>();
        body.put("startAt", startAt);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/admin/times")
                .then().statusCode(201);
    }

    private void createTheme(String name, String description, String thumbnailUrl) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("description", description);
        body.put("thumbnailUrl", thumbnailUrl);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/admin/themes")
                .then().statusCode(201);
    }

    private List<ReservationTime> getAvailableTimes(LocalDate date, Long themeId) {
        return RestAssured.given()
                .queryParam("date", date.toString())
                .queryParam("themeId", themeId)
                .when().get("/times/available-times")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", ReservationTime.class);
    }

    private void createMember(String name, String email, String rawPassword) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("rawPassword", rawPassword);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/members")
                .then().statusCode(204);
    }

    private void createReservation(String email, String password, Long memberId, LocalDate date, Long timeId,
                                   Long themeId) {
        String token = login(email, password);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("memberId", memberId);
        reservation.put("date", date.toString());
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(reservation)
                .when().post("/members/me/reservations")
                .then().statusCode(201);
    }

    private String login(String email, String password) {
        Map<String, Object> loginBody = Map.of(
                "email", email,
                "password", password
        );

        return "Bearer " + RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when().post("/auth/login")
                .then().statusCode(200)
                .extract()
                .jsonPath()
                .getString("accessToken");
    }
}
