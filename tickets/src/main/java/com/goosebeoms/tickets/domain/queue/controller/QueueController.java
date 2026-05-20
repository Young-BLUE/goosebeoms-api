package com.goosebeoms.tickets.domain.queue.controller;

import com.goosebeoms.tickets.domain.queue.dto.QueueStatusResponse;
import com.goosebeoms.tickets.domain.queue.service.QueueService;
import com.goosebeoms.tickets.domain.queue.service.SseEmitterRegistry;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.domain.user.repository.UserRepository;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import com.goosebeoms.tickets.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;
    private final SseEmitterRegistry sseRegistry;
    private final UserRepository userRepository;

    @PostMapping("/{scheduleId}/enter")
    public ApiResponse<QueueStatusResponse> enter(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(queueService.enter(scheduleId, userDetails.getUsername()));
    }

    @GetMapping("/{scheduleId}/status")
    public ApiResponse<QueueStatusResponse> status(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(queueService.status(scheduleId, userDetails.getUsername()));
    }

    @PostMapping("/{scheduleId}/leave")
    public ApiResponse<Void> leave(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        queueService.leave(scheduleId, userDetails.getUsername());
        return ApiResponse.ok();
    }

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
