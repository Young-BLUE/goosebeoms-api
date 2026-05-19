package com.goosebeoms.tickets.domain.auth.controller;

import com.goosebeoms.tickets.domain.auth.dto.AuthResponse;
import com.goosebeoms.tickets.domain.auth.dto.LoginRequest;
import com.goosebeoms.tickets.domain.auth.dto.SignupRequest;
import com.goosebeoms.tickets.domain.auth.service.AuthService;
import com.goosebeoms.tickets.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<AuthResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(authService.me(userDetails.getUsername()));
    }
}
