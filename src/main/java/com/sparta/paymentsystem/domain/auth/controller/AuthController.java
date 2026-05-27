package com.sparta.paymentsystem.domain.auth.controller;

import com.sparta.paymentsystem.domain.auth.dto.AuthResponse;
import com.sparta.paymentsystem.domain.auth.dto.LoginRequest;
import com.sparta.paymentsystem.domain.auth.dto.SignupRequest;
import com.sparta.paymentsystem.domain.auth.service.AuthService;
import com.sparta.paymentsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok(authService.login(request))
        );
    }
}
