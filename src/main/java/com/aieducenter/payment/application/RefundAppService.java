package com.aieducenter.payment.application;

import com.aieducenter.payment.application.dto.command.AuditRefundCommand;
import com.aieducenter.payment.application.dto.command.CreateRefundCommand;
import com.aieducenter.payment.application.dto.response.RefundOrderResponse;
import com.aieducenter.payment.domain.aggregate.PaymentOrder;
import com.aieducenter.payment.domain.aggregate.RefundOrder;
import com.aieducenter.payment.domain.enums.PaymentStatus;
import com.aieducenter.payment.domain.error.PaymentMessage;
import com.aieducenter.payment.domain.repository.PaymentOrderRepository;
import com.aieducenter.payment.domain.repository.RefundOrderRepository;
import com.cartisan.core.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 退款应用服务
 *
 * <p>负责退款相关的业务编排</p>
 */
@Service
@RequiredArgsConstructor
public class RefundAppService {

    private final RefundOrderRepository refundOrderRepository;
    private final PaymentOrderRepository paymentOrderRepository;

    /**
     * 创建退款订单
     */
    @Transactional
    public RefundOrderResponse createRefund(CreateRefundCommand command, String businessSystemName) {
        // 1. 查找原支付订单
        PaymentOrder paymentOrder = paymentOrderRepository.findByPaymentOrderNo(command.paymentOrderNo())
            .orElseThrow(() -> new ApplicationException(PaymentMessage.PAYMENT_ORDER_NOT_FOUND));

        // 2. 校验原订单支付成功
        if (paymentOrder.getStatus() != PaymentStatus.PAID) {
            throw new ApplicationException(PaymentMessage.ORIGINAL_PAYMENT_NOT_SUCCESS);
        }

        // 3. 创建退款订单
        RefundOrder refundOrder = new RefundOrder(
            command.businessOrderNo(),
            command.paymentOrderNo(),
            businessSystemName,
            paymentOrder.getBusinessType(),
            command.refundAmount(),
            paymentOrder.getAmount(), // 可退款金额 = 支付金额
            command.reason(),
            command.attach()
        );

        RefundOrder saved = refundOrderRepository.save(refundOrder);

        return toResponse(saved);
    }

    /**
     * 审核退款
     */
    @Transactional
    public RefundOrderResponse auditRefund(String refundOrderNo, AuditRefundCommand command) {
        RefundOrder refundOrder = refundOrderRepository.findByRefundOrderNo(refundOrderNo)
            .orElseThrow(() -> new ApplicationException(PaymentMessage.REFUND_ORDER_NOT_FOUND));

        refundOrder.audit(
            command.auditorId(),
            command.auditorName(),
            command.agreed(),
            command.remark()
        );

        RefundOrder saved = refundOrderRepository.save(refundOrder);

        return toResponse(saved);
    }

    /**
     * 查询退款订单
     */
    @Transactional(readOnly = true)
    public RefundOrderResponse getRefund(String refundOrderNo) {
        RefundOrder refundOrder = refundOrderRepository.findByRefundOrderNo(refundOrderNo)
            .orElseThrow(() -> new ApplicationException(PaymentMessage.REFUND_ORDER_NOT_FOUND));

        return toResponse(refundOrder);
    }

    private RefundOrderResponse toResponse(RefundOrder order) {
        return new RefundOrderResponse(
            order.getId(),
            order.getBusinessOrderNo(),
            order.getRefundOrderNo(),
            order.getPaymentOrderNo(),
            order.getBusinessSystemName(),
            order.getBusinessType(),
            order.getStatus(),
            order.getRefundAmount(),
            order.getRefundableAmount(),
            order.getReason(),
            order.getAuditorName(),
            order.getAuditAgreed(),
            order.getAuditRemark(),
            order.getCreatedAt(),
            order.getApprovedAt(),
            order.getRefundedAt(),
            order.getBankRefundNo()
        );
    }
}
