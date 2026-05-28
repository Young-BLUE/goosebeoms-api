package com.goosebeoms.tickets.domain.auth.controller;

import com.goosebeoms.tickets.domain.auth.dto.AuthResponse;
import com.goosebeoms.tickets.domain.auth.dto.LoginRequest;
import com.goosebeoms.tickets.domain.auth.dto.PasswordChangeRequest;
import com.goosebeoms.tickets.domain.auth.dto.RefreshRequest;
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

@Tag(name = "Auth", description = "회원가입 · 로그인 · 토큰 갱신 · 로그아웃 · 비밀번호 변경")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일/비밀번호/이름/전화번호로 가입 후 access+refresh 즉시 발급")
    @SecurityRequirements
    @PostMapping("/signup")
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok(authService.signup(request));
    }

    @Operation(summary = "로그인", description = "성공 시 accessToken(30분) + refreshToken(14일) 반환")
    @SecurityRequirements
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @Operation(summary = "토큰 갱신",
            description = "refresh 토큰으로 새 access+refresh 발급. RTR — 사용된 refresh는 즉시 폐기됨.")
    @SecurityRequirements
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request.refreshToken()));
    }

    @Operation(summary = "로그아웃",
            description = "refresh 토큰 폐기. access 토큰은 짧은 만료로 자연 소멸. 멱등.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ApiResponse.ok();
    }

    @Operation(summary = "내 정보")
    @GetMapping("/me")
    public ApiResponse<AuthResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(authService.me(userDetails.getUsername()));
    }

    @Operation(summary = "비밀번호 변경",
            description = "현재 비밀번호 검증 후 변경. 모든 refresh 토큰이 자동 무효화됨.")
    @PatchMapping("/me/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        authService.changePassword(userDetails.getUsername(), request);
        return ApiResponse.ok();
    }
}
