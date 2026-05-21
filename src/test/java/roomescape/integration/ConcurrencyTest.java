package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.global.exception.BusinessException;
import roomescape.integration.support.DatabaseHelper;
import roomescape.integration.support.SpringWebTest;
import roomescape.member.exception.DuplicateMemberException;
import roomescape.member.service.MemberCommand;
import roomescape.member.service.MemberService;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.theme.exception.DuplicateThemeException;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.service.ThemeService;
import roomescape.theme.service.dto.ThemeCommand;
import roomescape.time.exception.DuplicateTimeException;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.time.service.ReservationTimeCommand;
import roomescape.time.service.ReservationTimeService;

@SpringWebTest
class ConcurrencyTest {

    @Autowired
    DatabaseHelper databaseHelper;

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationTimeService reservationTimeService;

    @Autowired
    ThemeService themeService;

    @Autowired
    MemberService memberService;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @DisplayName("동일한 예약 요청이 동시에 들어오면 하나만 성공하고 나머지는 중복 예외가 발생한다")
    @Test
    void makeReservation() throws InterruptedException {
        //given
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");
        createMember("브라운", "example@gmail.com", "rawPassword");

        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> reservationService.makeReservation(new ReservationCommand(
                                1L,
                                LocalDate.of(2026, 5, 15),
                                1L,
                                1L
                        )
                ),
                100,
                DuplicateReservationException.class
        );

        //then
        assertThat(result.get(0)).isEqualTo(1);
        assertThat(result.get(1)).isEqualTo(99);
        assertThat(result.get(2)).isEqualTo(0);
    }

    private void createReservationTime(String startAt) {
        Map<String, Object> reservationTime = new HashMap<>();
        reservationTime.put("startAt", startAt);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(reservationTime)
                .when().post("/admin/times")
                .then().statusCode(201);
    }

    private void createTheme(String name, String description, String thumbnailUrl) {
        Map<String, Object> theme = new HashMap<>();
        theme.put("name", name);
        theme.put("description", description);
        theme.put("thumbnailUrl", thumbnailUrl);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().statusCode(201);
    }

    private void createMember(String name, String email, String rawPassword) {
        Map<String, Object> memberBody = new HashMap<>();
        memberBody.put("name", name);
        memberBody.put("email", email);
        memberBody.put("rawPassword", rawPassword);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(memberBody)
                .when().post("/members")
                .then().statusCode(204);
    }

    private List<Integer> runConcurrentlyAndCountResults(
            Runnable runnable,
            int numberOfThread,
            Class<? extends BusinessException> expectedExceptionType
    ) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);

        CountDownLatch latch = new CountDownLatch(numberOfThread);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger duplicateCount = new AtomicInteger();
        AtomicInteger unexpectedErrorCount = new AtomicInteger();

        for (int i = 0; i < numberOfThread; i++) {
            executorService.submit(() -> {
                try {
                    runnable.run();
                    successCount.incrementAndGet();
                } catch (Throwable throwable) {
                    if (expectedExceptionType.isInstance(throwable)) {
                        duplicateCount.incrementAndGet();
                    } else {
                        unexpectedErrorCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        return List.of(
                successCount.get(),
                duplicateCount.get(),
                unexpectedErrorCount.get()
        );
    }

    @DisplayName("동일한 예약 시간을 동시에 생성하면 하나만 성공하고 나머지는 중복 예외가 발생한다")
    @Test
    void registerTime() throws InterruptedException {
        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> reservationTimeService.registerReservationTime(
                        new ReservationTimeCommand(LocalTime.of(10, 0))
                ),
                100,
                DuplicateTimeException.class
        );

        //then
        assertThat(result.get(0)).isEqualTo(1);
        assertThat(result.get(1)).isEqualTo(99);
        assertThat(result.get(2)).isEqualTo(0);
    }

    @DisplayName("동일한 테마를 동시에 생성하면 하나만 성공하고 나머지는 중복 예외가 발생한다")
    @Test
    void registerTheme() throws InterruptedException {
        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> themeService.registerTheme(
                        new ThemeCommand("테마", "설명", "thumbnailUrl")
                ),
                100,
                DuplicateThemeException.class
        );

        //then
        assertThat(result.get(0)).isEqualTo(1);
        assertThat(result.get(1)).isEqualTo(99);
        assertThat(result.get(2)).isEqualTo(0);
    }

    @DisplayName("예약 삭제 요청이 동시에 들어오면 하나만 성공하고 나머지는 예외가 발생한다")
    @Test
    void deleteReservationById() throws InterruptedException {
        //given
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");
        createMember("브라운", "example@gmail.com", "rawPassword");

        String token = login("example@gmail.com", "rawPassword");
        createReservation(token, 1L, LocalDate.of(2026, 5, 15), 1L, 1L);

        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> reservationService.deleteReservationById(1L),
                100,
                ReservationNotFoundException.class
        );

        //then
        assertThat(result.get(0)).isEqualTo(1);
        assertThat(result.get(1)).isEqualTo(99);
        assertThat(result.get(2)).isEqualTo(0);
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

    private long createReservation(String token, Long memberId, LocalDate date, Long timeId, Long themeId) {
        Map<String, Object> body = new HashMap<>();
        body.put("memberId", memberId);
        body.put("date", date.toString());
        body.put("timeId", timeId);
        body.put("themeId", themeId);

        String location = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(body)
                .when().post("/members/me/reservations")
                .then().statusCode(201)
                .extract()
                .header("Location");

        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }

    @DisplayName("테마 삭제 요청이 동시에 들어오면 하나만 성공하고 나머지는 예외가 발생한다")
    @Test
    void removeThemeById() throws InterruptedException {
        //given
        createTheme("테마", "설명", "thumbnailUrl");

        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> themeService.removeThemeById(1L),
                100,
                ThemeNotFoundException.class
        );

        //then
        assertThat(result.get(0)).isEqualTo(1);
        assertThat(result.get(1)).isEqualTo(99);
        assertThat(result.get(2)).isEqualTo(0);
    }

    @DisplayName("예약 시간 삭제 요청이 동시에 들어오면 하나만 성공하고 나머지는 예외가 발생한다")
    @Test
    void removeReservationTimeById() throws InterruptedException {
        //given
        createReservationTime("10:00");

        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> reservationTimeService.removeReservationTimeById(1L),
                100,
                TimeNotFoundException.class
        );

        //then
        assertThat(result.get(0)).isEqualTo(1);
        assertThat(result.get(1)).isEqualTo(99);
        assertThat(result.get(2)).isEqualTo(0);
    }

    @DisplayName("서로 다른 본인 예약을 같은 슬롯으로 동시에 수정하면 하나만 성공하고 하나는 중복 예외가 발생한다")
    @Test
    void updateMyReservation() throws InterruptedException {
        //given
        createReservationTime("10:00");
        createReservationTime("11:00");
        createReservationTime("12:00");
        createTheme("테마", "설명", "thumbnailUrl");
        createMember("브라운", "example@gmail.com", "rawPassword");

        String token = login("example@gmail.com", "rawPassword");

        Long reservationId1 = createReservation(token, 1L, LocalDate.of(2026, 5, 15), 1L, 1L);
        Long reservationId2 = createReservation(token, 1L, LocalDate.of(2026, 5, 15), 2L, 1L);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger duplicateCount = new AtomicInteger();
        AtomicInteger unexpectedErrorCount = new AtomicInteger();

        //when
        List<Runnable> tasks = List.of(
                () -> reservationService.updateReservation(
                        new ReservationUpdateCommand(LocalDate.of(2026, 5, 16), 3L),
                        reservationId1
                ),
                () -> reservationService.updateReservation(
                        new ReservationUpdateCommand(LocalDate.of(2026, 5, 16), 3L),
                        reservationId2
                )
        );

        for (Runnable task : tasks) {
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    task.run();
                    successCount.incrementAndGet();
                } catch (DuplicateReservationException e) {
                    duplicateCount.incrementAndGet();
                } catch (Throwable throwable) {
                    unexpectedErrorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        //then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(duplicateCount.get()).isEqualTo(1);
        assertThat(unexpectedErrorCount.get()).isEqualTo(0);
    }

    @DisplayName("동일한 이메일의 회원을 동시에 가입하면 하나만 성공하고 나머지는 중복 예외가 발생한다")
    @Test
    void registerMember_concurrent() throws InterruptedException {
        //when
        int numberOfThread = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);

        CountDownLatch latch = new CountDownLatch(numberOfThread);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger duplicateCount = new AtomicInteger();
        AtomicInteger unexpectedErrorCount = new AtomicInteger();

        for (int i = 0; i < numberOfThread; i++) {
            executorService.submit(() -> {
                try {
                    memberService.signUp(new MemberCommand("이름", "example@gmail.com", "password"));
                    successCount.incrementAndGet();
                } catch (Throwable throwable) {
                    if (throwable instanceof DuplicateMemberException) {
                        duplicateCount.incrementAndGet();
                    } else {
                        unexpectedErrorCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        List<Integer> result = List.of(
                successCount.get(),
                duplicateCount.get(),
                unexpectedErrorCount.get()
        );

        //then
        assertThat(result.get(0)).isEqualTo(1);
        assertThat(result.get(1)).isEqualTo(99);
        assertThat(result.get(2)).isEqualTo(0);
    }
}
