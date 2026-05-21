package roomescape.integration;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.integration.support.DatabaseHelper;
import roomescape.integration.support.SpringWebTest;

@SpringWebTest
public class ThirdMissionStepTest {

    @Autowired
    DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @Test
    void 시간_관리_API() {
        Map<String, String> body = new HashMap<>();
        body.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/admin/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 예약과_시간_연결() {
        Map<String, Object> memberBody = new HashMap<>();
        memberBody.put("name", "브라운");
        memberBody.put("email", "example@gmail.com");
        memberBody.put("rawPassword", "rawPassword");

        Map<String, Object> themeBody = new HashMap<>();
        themeBody.put("name", "우아한 테마");
        themeBody.put("description", "우아한테크코스 전용 테마입니다.");
        themeBody.put("thumbnailUrl", "https://example.com/image.png");

        Map<String, Object> reservationBody = new HashMap<>();
        reservationBody.put("memberId", 1);
        reservationBody.put("date", "2026-05-05");
        reservationBody.put("timeId", 1);
        reservationBody.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body("{\"startAt\": \"10:00\"}")
                .when().post("/admin/times")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeBody)
                .when().post("/admin/themes")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberBody)
                .when().post("/members")
                .then().statusCode(204);

        String token = login("example@gmail.com", "rawPassword");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(reservationBody)
                .when().post("/members/me/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
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

    @Test
    void 날짜가_없으면_예약_생성_실패() {
        Map<String, Object> memberBody = new HashMap<>();
        memberBody.put("name", "브라운");
        memberBody.put("email", "example@gmail.com");
        memberBody.put("rawPassword", "rawPassword");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberBody)
                .when().post("/members")
                .then().statusCode(204);

        String token = login("example@gmail.com", "rawPassword");

        Map<String, Object> body = new HashMap<>();
        body.put("memberId", 1L);
        body.put("timeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(body)
                .when().post("/members/me/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 잘못된_시간_형식으로_시간_생성_실패() {
        Map<String, String> body = new HashMap<>();
        body.put("startAt", "오전 10시");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 존재하지_않는_시간_ID로_예약_생성_실패() {
        Map<String, Object> memberBody = new HashMap<>();
        memberBody.put("name", "브라운");
        memberBody.put("email", "example@gmail.com");
        memberBody.put("rawPassword", "rawPassword");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberBody)
                .when().post("/members")
                .then().statusCode(204);

        String token = login("example@gmail.com", "rawPassword");

        Map<String, Object> body = new HashMap<>();
        body.put("memberId", 1L);
        body.put("date", "2023-08-05");
        body.put("timeId", 999L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(body)
                .when().post("/members/me/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 중복된_날짜와_시간으로_예약_생성_실패() {
        Map<String, Object> memberBody = new HashMap<>();
        memberBody.put("name", "브라운");
        memberBody.put("email", "example@gmail.com");
        memberBody.put("rawPassword", "rawPassword");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberBody)
                .when().post("/members")
                .then().statusCode(204);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"startAt\": \"10:00\"}")
                .when().post("/admin/times")
                .then().statusCode(201);

        Map<String, Object> theme = new HashMap<>();
        theme.put("name", "우아한 테마");
        theme.put("description", "우아한테크코스 전용 테마입니다.");
        theme.put("thumbnailUrl", "https://example.com/image.png");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().statusCode(201);

        String token = login("example@gmail.com", "rawPassword");

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("memberId", 1L);
        reservation.put("date", "2026-05-05");
        reservation.put("timeId", 1L);
        reservation.put("themeId", 1L);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(reservation)
                .when().post("/members/me/reservations")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(reservation)
                .when().post("members/me/reservations")
                .then().log().all()
                .statusCode(409);
    }
}
