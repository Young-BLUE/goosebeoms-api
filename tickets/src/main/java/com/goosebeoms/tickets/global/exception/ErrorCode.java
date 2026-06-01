package com.goosebeoms.tickets.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SHOW_NOT_FOUND("공연을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SCHEDULE_NOT_FOUND("공연 회차를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SEAT_NOT_AVAILABLE("선택할 수 없는 좌석입니다.", HttpStatus.CONFLICT),
    SEAT_CONFLICT("다른 사용자가 동일한 좌석을 선택했습니다. 다시 시도해주세요.", HttpStatus.CONFLICT),
    INSUFFICIENT_SEATS("잔여 좌석이 부족합니다.", HttpStatus.CONFLICT),

    EMAIL_DUPLICATED("이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS("이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_REFRESH_TOKEN("유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),
    PASSWORD_MISMATCH("현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

    BOOKING_NOT_FOUND("예매 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BOOKING_ALREADY_CANCELLED("이미 취소된 예매입니다.", HttpStatus.CONFLICT),
    BOOKING_NOT_PAYABLE("결제 가능한 상태가 아닙니다.", HttpStatus.CONFLICT),
    BOOKING_HOLD_EXPIRED("좌석 점유 시간이 만료됐습니다.", HttpStatus.CONFLICT),

    PAYMENT_FAILED("결제에 실패했습니다.", HttpStatus.PAYMENT_REQUIRED),
    PAYMENT_NOT_FOUND("결제 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PAYMENT_ORDER_MISMATCH("주문번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_AMOUNT_MISMATCH("결제 금액이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_CANCEL_FAILED("결제 환불에 실패했습니다. 잠시 후 다시 시도해주세요.", HttpStatus.BAD_GATEWAY),

    COUPON_NOT_FOUND("쿠폰을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COUPON_EXHAUSTED("쿠폰이 모두 소진됐습니다.", HttpStatus.CONFLICT),
    COUPON_ALREADY_ISSUED("이미 발급받은 쿠폰입니다.", HttpStatus.CONFLICT),
    COUPON_NOT_AVAILABLE("사용할 수 없는 쿠폰입니다.", HttpStatus.BAD_REQUEST),

    QUEUE_TOKEN_REQUIRED("대기열 통과 토큰이 필요합니다.", HttpStatus.BAD_REQUEST),
    QUEUE_TOKEN_EXPIRED("대기열 통과 토큰이 만료됐습니다.", HttpStatus.UNAUTHORIZED),
    QUEUE_TOKEN_MISMATCH("대기열 토큰의 회차/사용자가 일치하지 않습니다.", HttpStatus.FORBIDDEN),
    QUEUE_NOT_ACTIVE("아직 대기열 진입 차례가 아닙니다.", HttpStatus.FORBIDDEN),

    NOTIFICATION_NOT_FOUND("알림을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    TOO_MANY_REQUESTS("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.", HttpStatus.TOO_MANY_REQUESTS);

    private final String message;
    private final HttpStatus httpStatus;
}
