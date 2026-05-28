package com.goosebeoms.tickets.domain.notification.controller;

import com.goosebeoms.tickets.domain.notification.dto.NotificationResponse;
import com.goosebeoms.tickets.domain.notification.service.NotificationService;
import com.goosebeoms.tickets.domain.notification.sse.NotificationSseRegistry;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.domain.user.repository.UserRepository;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import com.goosebeoms.tickets.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Tag(name = "Notifications", description = "인앱 알림 목록 · 읽음 처리 · SSE 구독")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationSseRegistry sseRegistry;
    private final UserRepository userRepository;

    @Operation(summary = "내 알림 목록 (페이지)")
    @GetMapping
    public ApiResponse<Page<NotificationResponse>> list(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(notificationService.list(userDetails.getUsername(), pageable));
    }

    @Operation(summary = "안 읽은 알림 개수")
    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(Map.of("count", notificationService.unreadCount(userDetails.getUsername())));
    }

    @Operation(summary = "단일 알림 읽음 처리")
    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        notificationService.markRead(id, userDetails.getUsername());
        return ApiResponse.ok();
    }

    @Operation(summary = "전체 읽음 처리")
    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllRead(@AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllRead(userDetails.getUsername());
        return ApiResponse.ok();
    }

    @Operation(summary = "알림 SSE 구독",
            description = "text/event-stream. 새 알림 생성 시 'notification' 이벤트로 push. EventSource는 헤더 안 되므로 ?token=accessToken 쿼리로 인증.")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        SseEmitter emitter = new SseEmitter(0L);
        sseRegistry.register(user.getId(), emitter);
        return emitter;
    }
}
