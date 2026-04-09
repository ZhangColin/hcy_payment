package com.aieducenter.payment.application.dto.response;

import com.aieducenter.payment.domain.enums.RefundStatus;

import java.time.LocalDateTime;

/**
 * 退款订单响应
 */
public record RefundOrderResponse(
    Long id,
    String businessOrderNo,
    String refundOrderNo,
    String paymentOrderNo,
    String businessSystemName,
    String businessName,
    RefundStatus status,
    Long refundAmount,
    Long refundableAmount,
    String reason,
    String auditorName,
    Boolean auditAgreed,
    String auditRemark,
    LocalDateTime createdAt,
    LocalDateTime approvedAt,
    LocalDateTime refundedAt,
    String bankRefundNo
) {
}
