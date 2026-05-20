package roomescape.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.controller.dto.LoginRequest;
import roomescape.auth.controller.dto.LoginResponse;
import roomescape.auth.service.AuthService;
import roomescape.auth.service.dto.LoginResult;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResult loginResult = authService.login(request.toCommand());
        LoginResponse response = LoginResponse.from(loginResult);
        return ResponseEntity.ok(response);
    }
}
