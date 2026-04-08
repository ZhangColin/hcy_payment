package com.aieducenter.payment.application.dto.response;

import com.aieducenter.payment.domain.enums.BusinessType;
import com.aieducenter.payment.domain.enums.PaymentMethod;
import com.aieducenter.payment.domain.enums.PaymentStatus;

import java.time.LocalDateTime;

/**
 * 支付订单响应
 *
 * <p>返回给业务系统的支付订单详情</p>
 */
public record PaymentOrderResponse(
    /**
     * 订单 ID
     */
    Long id,

    /**
     * 业务订单号
     */
    String businessOrderNo,

    /**
     * 支付订单号
     */
    String paymentOrderNo,

    /**
     * 业务系统名称
     */
    String businessSystemName,

    /**
     * 业务类型
     */
    BusinessType businessType,

    /**
     * 支付状态
     */
    PaymentStatus status,

    /**
     * 支付金额（分）
     */
    Long amount,

    /**
     * 币种
     */
    String currency,

    /**
     * 支付标题
     */
    String subject,

    /**
     * 支付描述
     */
    String body,

    /**
     * 支付方式
     */
    PaymentMethod paymentMethod,

    /**
     * 二维码 URL
     * <p>用户扫码支付的地址</p>
     */
    String qrCodeUrl,

    /**
     * 客户端 IP
     */
    String clientIp,

    /**
     * 创建时间
     */
    LocalDateTime createdAt,

    /**
     * 过期时间
     */
    LocalDateTime expiredAt,

    /**
     * 支付完成时间
     */
    LocalDateTime paidAt,

    /**
     * 银行订单号
     */
    String bankOrderNo,

    /**
     * 第三方订单号
     */
    String thirdPartyOrderNo
) {
}
