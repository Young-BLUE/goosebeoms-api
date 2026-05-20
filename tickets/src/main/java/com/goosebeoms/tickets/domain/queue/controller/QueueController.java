package com.goosebeoms.tickets.domain.queue.controller;

import com.goosebeoms.tickets.domain.queue.dto.QueueStatusResponse;
import com.goosebeoms.tickets.domain.queue.service.QueueService;
import com.goosebeoms.tickets.domain.queue.service.SseEmitterRegistry;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.domain.user.repository.UserRepository;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import com.goosebeoms.tickets.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Queue", description = "대기열 진입 · 상태 조회 · SSE 구독")
@RestController
@RequestMapping("/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;
    private final SseEmitterRegistry sseRegistry;
    private final UserRepository userRepository;

    @Operation(summary = "대기열 진입", description = "ZADD NX 멱등 진입. 이미 active면 현재 토큰 반환")
    @PostMapping("/{scheduleId}/enter")
    public ApiResponse<QueueStatusResponse> enter(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(queueService.enter(scheduleId, userDetails.getUsername()));
    }

    @Operation(summary = "대기열 상태", description = "polling fallback용. 활성 승격되면 token 동봉")
    @GetMapping("/{scheduleId}/status")
    public ApiResponse<QueueStatusResponse> status(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(queueService.status(scheduleId, userDetails.getUsername()));
    }

    @Operation(summary = "대기열 이탈", description = "wait set에서 자발적 제거")
    @PostMapping("/{scheduleId}/leave")
    public ApiResponse<Void> leave(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        queueService.leave(scheduleId, userDetails.getUsername());
        return ApiResponse.ok();
    }

    @Operation(summary = "SSE 구독", description = "text/event-stream. rank 변화/승격(ready) 이벤트 push")
    @GetMapping(value = "/{scheduleId}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        SseEmitter emitter = new SseEmitter(0L);
        sseRegistry.register(scheduleId, user.getId(), emitter);

        QueueStatusResponse snapshot = queueService.status(scheduleId, userDetails.getUsername());
        sseRegistry.send(scheduleId, user.getId(), "status", snapshot);
        return emitter;
    }
}
