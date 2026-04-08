package com.aieducenter.payment.domain.aggregate;

import cn.hutool.core.util.StrUtil;
import com.aieducenter.payment.domain.enums.BusinessType;
import com.aieducenter.payment.domain.enums.PaymentMethod;
import com.aieducenter.payment.domain.enums.PaymentStatus;
import com.aieducenter.payment.domain.error.PaymentMessage;
import com.cartisan.core.stereotype.Aggregate;
import com.cartisan.core.util.Assertions;
import com.cartisan.data.jpa.domain.AuditableSoftDeletable;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_payment_orders")
@Aggregate
public class PaymentOrder extends AuditableSoftDeletable {
    private static final String PAYMENT_ORDER_NO_PREFIX = "PAY";

    @Getter
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Getter
    @Column(name = "business_order_no", nullable = false, length = 64)
    private String businessOrderNo;

    @Getter
    @Column(name = "payment_order_no", nullable = false, unique = true, length = 64)
    private String paymentOrderNo;

    @Getter
    @Column(name = "business_system_name", nullable = false, length = 128)
    private String businessSystemName;

    @Getter
    @Column(name = "business_type", nullable = false)
    private BusinessType businessType;

    @Getter
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Getter
    @Column(name = "amount", nullable = false)
    private Long amount;

    @Getter
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Getter
    @Column(name = "subject")
    private String subject;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Getter
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Getter
    @Column(name = "client_ip", length = 64)
    private String clientIp;

    @Getter
    @Column(name = "notify_url", length = 512)
    private String notifyUrl;

    @Getter
    @Column(name = "return_url", length = 512)
    private String returnUrl;

    @Getter
    @Column(name = "attach", columnDefinition = "TEXT")
    private String attach;

    @Getter
    @Column(name = "expired_seconds")
    private Long expiredSeconds;

    // Timestamps for status transitions
    @Getter
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Getter
    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Getter
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Getter
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    // Bank information
    @Getter
    @Column(name = "bank_order_no", length = 64)
    private String bankOrderNo;

    @Getter
    @Column(name = "third_party_order_no", length = 128)
    private String thirdPartyOrderNo;

    @PrePersist
    void prePersist() {
        if (id == null) {
            this.id = com.cartisan.data.jpa.id.TsidGenerator.newInstance().generate();
            this.paymentOrderNo = generatePaymentOrderNo();
            this.status = PaymentStatus.PENDING;
            this.currency = "CNY";
            this.expiredAt = calculateExpiredAt();
        }
    }

    /**
     * 创建支付订单
     */
    public PaymentOrder(
            String businessOrderNo,
            String businessSystemName,
            BusinessType businessType,
            Long amount,
            String subject,
            String body,
            String clientIp,
            String notifyUrl,
            String returnUrl,
            String attach,
            Long expiredSeconds
    ) {
        Assertions.require(StrUtil.isNotBlank(businessOrderNo),
            PaymentMessage.AMOUNT_INVALID);
        Assertions.require(amount != null && amount > 0,
            PaymentMessage.AMOUNT_INVALID);

        this.businessOrderNo = businessOrderNo;
        this.businessSystemName = businessSystemName;
        this.businessType = businessType;
        this.amount = amount;
        this.subject = subject;
        this.body = body;
        this.clientIp = clientIp;
        this.notifyUrl = notifyUrl;
        this.returnUrl = returnUrl;
        this.attach = attach;
        this.expiredSeconds = expiredSeconds != null ? expiredSeconds : 3600L; // 默认1小时
    }

    /**
     * 支付成功
     */
    public void markAsPaid(String bankOrderNo, String thirdPartyOrderNo, PaymentMethod paymentMethod) {
        Assertions.require(this.status == PaymentStatus.PENDING,
            PaymentMessage.PAYMENT_ORDER_NOT_PENDING);

        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.bankOrderNo = bankOrderNo;
        this.thirdPartyOrderNo = thirdPartyOrderNo;
        this.paymentMethod = paymentMethod;
    }

    /**
     * 支付失败
     */
    public void markAsFailed(String reason) {
        Assertions.require(this.status == PaymentStatus.PENDING,
            PaymentMessage.PAYMENT_ORDER_NOT_PENDING);

        this.status = PaymentStatus.FAILED;
        this.failedAt = LocalDateTime.now();
    }

    /**
     * 取消支付
     */
    public void cancel() {
        Assertions.require(this.status == PaymentStatus.PENDING,
            PaymentMessage.PAYMENT_ORDER_NOT_PENDING);

        this.status = PaymentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * 标记为过期
     */
    public void markAsExpired() {
        Assertions.require(this.status == PaymentStatus.PENDING,
            PaymentMessage.PAYMENT_ORDER_NOT_PENDING);

        this.status = PaymentStatus.EXPIRED;
    }

    /**
     * 检查是否过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }

    private String generatePaymentOrderNo() {
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = cn.hutool.core.util.RandomUtil.randomString("0123456789", 6);
        return PAYMENT_ORDER_NO_PREFIX + timestamp + random;
    }

    private LocalDateTime calculateExpiredAt() {
        return LocalDateTime.now().plusSeconds(this.expiredSeconds != null ? this.expiredSeconds : 3600L);
    }
}
