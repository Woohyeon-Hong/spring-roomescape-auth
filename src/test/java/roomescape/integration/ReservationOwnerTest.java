package roomescape.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import java.sql.Time;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.integration.support.DatabaseHelper;
import roomescape.integration.support.SpringWebTest;

@SpringWebTest
public class ReservationOwnerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();

        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(LocalTime.of(10, 0))
        );

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마", "설명", "thumbnailUrl"
        );

        Map<String, Object> memberCreateBody = Map.of(
                "name", "브라운",
                "email", "brown@gmail.com",
                "rawPassword", "rawPassword"
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(memberCreateBody)
                .when().post("/members")
                .then().statusCode(204);
    }

    @DisplayName("인증한 사용자만 예약을 생성하고 201을 반환한다.")
    @Test
    void createReservationTest_success() {
        //given
        String token = login("brown@gmail.com", "rawPassword");

        Map<String, Object> body = new HashMap<>();
        body.put("memberId", 1L);
        body.put("date", "2026-05-01");
        body.put("timeId", 1L);
        body.put("themeId", 1L);

        //when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(body)
                .when().post("/members/me/reservations")
                .then().statusCode(201);
    }

    private String login(String email, String password) {
        Map<String, Object> loginBody = Map.of(
                "email", email,
                "password", password
        );

        JsonPath jsonPath = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when().post("/auth/login")
                .then().statusCode(200)
                .extract()
                .jsonPath();

        return jsonPath.getString("tokenType") + " " + jsonPath.getString("accessToken");
    }

    @DisplayName("인증받지 않은 사용자가 예약을 생성하면 401을 반환한다.")
    @Test
    void createReservationTest_fail() {
        //given
        Map<String, Object> body = new HashMap<>();
        body.put("memberId", 1L);
        body.put("date", "2026-05-01");
        body.put("timeId", 1L);
        body.put("themeId", 1L);

        //when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/members/me/reservations")
                .then().statusCode(401);
    }

    @DisplayName("본인의 예약을 수정하면 204를 반환한다.")
    @Test
    void updateMyReservationTest_success() {
        //given
        String token = login("brown@gmail.com", "rawPassword");

        Map<String, Object> createReservationBody = new HashMap<>();
        createReservationBody.put("memberId", 1L);
        createReservationBody.put("date", "2026-05-01");
        createReservationBody.put("timeId", 1L);
        createReservationBody.put("themeId", 1L);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(createReservationBody)
                .when().post("/members/me/reservations")
                .then().statusCode(201);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-05-02");

        //when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(body)
                .when().patch("/members/me/reservations/1")
                .then().statusCode(204);
    }

    @DisplayName("본인 것이 아닌 예약을 수정하면 403를 반환한다.")
    @Test
    void updateMyReservationTest_fail() {
        //given
        Map<String, Object> memberCreateBody = Map.of(
                "name", "포비",
                "email", "pobi@gmail.com",
                "rawPassword", "rawPassword"
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(memberCreateBody)
                .when().post("/members")
                .then().statusCode(204);

        String pobiToken = login("pobi@gmail.com", "rawPassword");
        String brownToken = login("brown@gmail.com", "rawPassword");

        Map<String, Object> createReservationBody = new HashMap<>();
        createReservationBody.put("memberId", 1L);
        createReservationBody.put("date", "2026-05-01");
        createReservationBody.put("timeId", 1L);
        createReservationBody.put("themeId", 1L);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", brownToken)
                .body(createReservationBody)
                .when().post("/members/me/reservations")
                .then().statusCode(201);

        //when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", pobiToken)
                .body(createReservationBody)
                .when().patch("/members/me/reservations/1")
                .then().statusCode(403);
    }

    @DisplayName("본인의 예약을 삭제하면 204를 반환한다.")
    @Test
    void deleteMyReservationTest_success() {
        //given
        String token = login("brown@gmail.com", "rawPassword");

        Map<String, Object> createReservationBody = new HashMap<>();
        createReservationBody.put("memberId", 1L);
        createReservationBody.put("date", "2026-05-01");
        createReservationBody.put("timeId", 1L);
        createReservationBody.put("themeId", 1L);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(createReservationBody)
                .when().post("/members/me/reservations")
                .then().statusCode(201);

        //when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .when().delete("/members/me/reservations/1")
                .then().statusCode(204);
    }

    @DisplayName("본인 것이 아닌 예약을 삭제하면 403를 반환한다.")
    @Test
    void deleteMyReservationTest_fail() {
        //given
        Map<String, Object> memberCreateBody = Map.of(
                "name", "포비",
                "email", "pobi@gmail.com",
                "rawPassword", "rawPassword"
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(memberCreateBody)
                .when().post("/members")
                .then().statusCode(204);

        String pobiToken = login("pobi@gmail.com", "rawPassword");
        String brownToken = login("brown@gmail.com", "rawPassword");

        Map<String, Object> createReservationBody = new HashMap<>();
        createReservationBody.put("memberId", 1L);
        createReservationBody.put("date", "2026-05-01");
        createReservationBody.put("timeId", 1L);
        createReservationBody.put("themeId", 1L);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", brownToken)
                .body(createReservationBody)
                .when().post("/members/me/reservations")
                .then().statusCode(201);

        //when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", pobiToken)
                .body(createReservationBody)
                .when().delete("/members/me/reservations/1")
                .then().statusCode(403);
    }
}
