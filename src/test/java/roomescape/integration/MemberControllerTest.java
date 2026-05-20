package roomescape.integration;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.integration.support.DatabaseHelper;
import roomescape.integration.support.SpringWebTest;

@SpringWebTest
public class MemberControllerTest {

    @Autowired
    DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @DisplayName("회원 가입에 성공하면 204를 반환한다.")
    @Test
    void createMember_success() {
        //given
        Map<String, Object> params = Map.of(
            "name", "브라운",
            "email", "example@gmail.com",
            "rawPassword", "rawPassword"
        );

        //when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/members")
                .then().statusCode(204);
    }

    @DisplayName("회원 가입 시, 이메일이 중복되면 409를 반환한다.")
    @Test
    void createMember_duplicate_email() {
        //given
        Map<String, Object> params = Map.of(
                "name", "브라운",
                "email", "example@gmail.com",
                "rawPassword", "rawPassword"
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/members")
                .then().statusCode(204);

        //when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/members")
                .then().statusCode(409)
                .body("message", equalTo("이미 회원이 가입돼 있습니다."));
    }

    @DisplayName("회원 가입 시, 잘못된 형식으로 요청하면 400를 반환한다.")
    @Test
    void createMember_invalidRequestFormat() {
        //given
        Map<String, Object> valid = Map.of(
                "name", "브라운",
                "email", "example@gmail.com",
                "rawPassword", "rawPassword"
        );

        Map<String, Object> withoutName = new HashMap<>(valid);
        withoutName.put("name", null);

        Map<String, Object> blankName = new HashMap<>(valid);
        blankName.put("name", "");

        Map<String, Object> withoutEmail = new HashMap<>(valid);
        withoutEmail.put("email", null);

        Map<String, Object> blankEmail = new HashMap<>(valid);
        blankEmail.put("email", "");

        Map<String, Object> invalidEmail = new HashMap<>(valid);
        invalidEmail.put("email", "invalid_email");

        Map<String, Object> withoutPassword = new HashMap<>(valid);
        withoutPassword.put("rawPassword", null);

        Map<String, Object> blankPassword = new HashMap<>(valid);
        blankPassword.put("rawPassword", "");

        //when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(withoutName)
                .when().post("/members")
                .then().statusCode(400)
                .body("message", equalTo("회원 요청 형식이 유효하지 않습니다."));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(withoutEmail)
                .when().post("/members")
                .then().statusCode(400)
                .body("message", equalTo("이메일 형식이 유효하지 않습니다."));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(withoutPassword)
                .when().post("/members")
                .then().statusCode(400)
                .body("message", equalTo("회원 요청 형식이 유효하지 않습니다."));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(blankName)
                .when().post("/members")
                .then().statusCode(400)
                .body("message", equalTo("회원 요청 형식이 유효하지 않습니다."));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(blankEmail)
                .when().post("/members")
                .then().statusCode(400)
                .body("message", equalTo("이메일 형식이 유효하지 않습니다."));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(blankPassword)
                .when().post("/members")
                .then().statusCode(400)
                .body("message", equalTo("회원 요청 형식이 유효하지 않습니다."));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(invalidEmail)
                .when().post("/members")
                .then().statusCode(400)
                .body("message", equalTo("이메일 형식이 유효하지 않습니다."));
    }
}
