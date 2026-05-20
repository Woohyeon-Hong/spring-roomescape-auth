package roomescape.integration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.integration.support.DatabaseHelper;
import roomescape.integration.support.SpringWebTest;

@SpringWebTest
public class AuthControllerTest {

    @Autowired
    DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @DisplayName("로그인을 성공하면 200을 반환한다.")
    @Test
    void loginTest_success() {
        //given
        Map<String, Object> memberCreateBody = Map.of(
                "name", "브라운",
                "email", "example@gmail.com",
                "rawPassword", "rawPassword"
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(memberCreateBody)
                .when().post("/members")
                .then().statusCode(204);

        Map<String, Object> loginBody = Map.of(
                "email", "example@gmail.com",
                "password", "rawPassword"
        );

        //when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when().post("/auth/login")
                .then().statusCode(200)
                .body("accessToken", notNullValue())
                .body("tokenType", equalTo("BEARER"))
                .body("expiresIn", equalTo(3600));
    }

    @DisplayName("로그인 시 비밀번호가 다르면 401이 반환된다.")
    @Test
    void loginTest_different_password() {
        //given
        Map<String, Object> memberCreateBody = Map.of(
                "name", "브라운",
                "email", "example@gmail.com",
                "rawPassword", "rawPassword"
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(memberCreateBody)
                .when().post("/members")
                .then().statusCode(204);

        Map<String, Object> loginBody = Map.of(
                "email", "example@gmail.com",
                "password", "other"
        );

        //when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when().post("/auth/login")
                .then().statusCode(401);
    }
}
