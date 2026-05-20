package com.goosebeoms.tickets.domain.auth.controller;

import com.goosebeoms.tickets.domain.auth.dto.AuthResponse;
import com.goosebeoms.tickets.domain.auth.dto.LoginRequest;
import com.goosebeoms.tickets.domain.auth.dto.SignupRequest;
import com.goosebeoms.tickets.domain.auth.service.AuthService;
import com.goosebeoms.tickets.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "회원가입 · 로그인 · 내 정보")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일/비밀번호/이름/전화번호로 가입 후 JWT 즉시 발급")
    @SecurityRequirements
    @PostMapping("/signup")
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok(authService.signup(request));
    }

    @Operation(summary = "로그인", description = "성공 시 JWT 토큰 반환 (Authorization: Bearer ... 형식)")
    @SecurityRequirements
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @Operation(summary = "내 정보")
    @GetMapping("/me")
    public ApiResponse<AuthResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(authService.me(userDetails.getUsername()));
    }
}
