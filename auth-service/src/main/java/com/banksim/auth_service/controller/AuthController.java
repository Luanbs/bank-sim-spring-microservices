package com.banksim.auth_service.controller;

import com.banksim.auth_service.dto.GeneratedToken;
import com.banksim.auth_service.dto.request.LoginRequest;
import com.banksim.auth_service.dto.request.RegisterRequest;
import com.banksim.auth_service.dto.response.CreateUserResponse;
import com.banksim.auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping()
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public GeneratedToken login(@Valid @RequestBody LoginRequest loginRequest) {

        return authService.login(loginRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<CreateUserResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        var response = authService.registerUser(registerRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("sub", jwt.getSubject(),
                "username", jwt.getClaimAsString("username"),
                "roles", jwt.getClaimAsStringList("roles"),
                "issuer", jwt.getIssuer().toString(),
                "expiresAt", jwt.getExpiresAt());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {
        authService.logout(jwt);
        return ResponseEntity.noContent().build();
    }
}
