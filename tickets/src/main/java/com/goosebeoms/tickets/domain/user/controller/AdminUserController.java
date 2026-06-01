package com.goosebeoms.tickets.domain.user.controller;

import com.goosebeoms.tickets.domain.user.dto.AdminUserResponse;
import com.goosebeoms.tickets.domain.user.service.AdminUserService;
import com.goosebeoms.tickets.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin / Users", description = "관리자 사용자 검색·조회")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "사용자 검색", description = "q는 email/name 부분 일치 (대소문자 무시)")
    @GetMapping
    public ApiResponse<Page<AdminUserResponse>> search(
            @Parameter(description = "email 또는 name 키워드") @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.ok(adminUserService.search(q, pageable));
    }

    @Operation(summary = "사용자 단건 조회")
    @GetMapping("/{userId}")
    public ApiResponse<AdminUserResponse> get(@PathVariable Long userId) {
        return ApiResponse.ok(adminUserService.get(userId));
    }
}
