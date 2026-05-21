# 방탈출 인증과 인가

## 1단계 요구사항 - 웹에서 로그인하기

### 1. 회원 가입

#### 구현

- [x] 구현 완료

#### 메서드 / URL

- POST /members
    - 새로운 회원을 생성하는 것이기 때문에, `/members`에 대해 POST를 사용하도록 한다.

#### 요청

```json
{
  name,
  email,
  password
}
```

#### 응답

- 204 No Content
    - 회원가입은 리소스를 생성하는 작업이지만, 현재는 생성된 회원을 조회할 URI를 제공하지 않으므로 Location 헤더를 반환하지 않는다.
    - 또한 응답 본문에 포함할 추가 정보가 없으므로, 본문 없이 204 No Content를 반환한다.

### 2. 회원 탈퇴

#### 구현

- [x] 구현 완료

#### 메서드 / URL

- DELETE /members/me
    - 회원을 삭제하는 것이기 때문에, `/members`에 대해 DELETE을 사용하도록 한다.

#### 응답

- 204 No Content
    - 회원 삭제에 대해서는 반환할 데이터가 없다.

### 3. 로그인

#### 구현

- [x] 구현 완료

#### 메서드 / URL

- POST /auth/login
    - 소셜 로그인, OTP, 토큰 재발급, 로그아웃 등의 확장성을 고려하면 /auth 하위 경로가 자연스럽다.
    - 또한 로그인은 “회원 리소스 생성/수정”이 아니라 “인증 절차”라서 auth가 더 자연스럽다.

#### 요청

```json
{
  email,
  password
}
```

#### 응답

- 200 Ok
    - 로그인은 인증 처리의 성공 결과로 액세스 토큰을 즉시 반환해야 하므로, 응답 본문을 포함하는 200 OK를 사용한다.
    - 클라이언트는 응답 본문의 accessToken, tokenType, expiresIn을 사용해 인증 헤더를 구성하고, 만료 시점을 예측해 재인증/재발급 흐름을 처리할 수 있다.

```json
{
  accessToken,
  tokenType,
  expiresIn
}
```

### 4. 예약 생성

#### 구현

- [x] 구현 완료

#### 메서드 / URL

- POST /members/me/reservations
    - 본인 예약 생성 API이므로, 일반 예약 URI와 분리해 /members/me 하위 경로를 사용한다.

### 5. 예약 조회

#### 구현

- [x] 구현 완료

#### 메서드 / URL

- PATCH /members/me/reservations/{reservationId}
    - 본인 예약 수정 API이므로, 생성 API와 동일한 맥락에서 /members/me 하위 경로를 사용한다.

### 6. 예약 삭제

#### 구현

- [x] 구현 완료

#### 메서드 / URL

- DELETE /members/me/reservations/{reservationId}
    - 본인 예약 삭제 API이므로, 수정 API와 동일한 맥락에서 /members/me 하위 경로를 사용한다.

