package com.aieducenter.payment.domain.port.response;

import com.aieducenter.payment.domain.enums.PaymentMethod;
import com.aieducenter.payment.domain.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

/**
 * 查询支付响应
 *
 * <p>支付网关返回的支付状态查询结果</p>
 */
@Data
@Builder
public class QueryPaymentResponse {
    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 返回码
     */
    private String returnCode;

    /**
     * 返回消息
     */
    private String returnMsg;

    /**
     * 支付状态
     */
    private PaymentStatus paymentStatus;

    /**
     * 订单总金额（分）
     */
    private Long amount;

    /**
     * 已支付金额（分）
     */
    private Long paidAmount;

    /**
     * 支付时间
     */
    private String payTime;

    /**
     * 银行订单号
     */
    private String bankOrderNo;

    /**
     * 第三方订单号
     * <p>如微信、支付宝的订单号</p>
     */
    private String thirdPartyOrderNo;

    /**
     * 支付方式
     */
    private PaymentMethod paymentMethod;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionTime;
}
