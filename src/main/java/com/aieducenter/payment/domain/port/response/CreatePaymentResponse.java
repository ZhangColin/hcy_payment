package com.aieducenter.payment.domain.port.response;

import lombok.Builder;
import lombok.Data;

/**
 * 创建支付响应
 *
 * <p>支付网关返回的创建支付结果</p>
 */
@Data
@Builder
public class CreatePaymentResponse {
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
     * 二维码URL
     * <p>用户扫码支付的二维码地址</p>
     */
    private String qrCodeUrl;

    /**
     * 银行订单号
     */
    private String bankOrderNo;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionTime;

    /**
     * 请求参数（JSON格式，用于日志记录）
     */
    private String requestParams;

    /**
     * 响应体（JSON格式，用于日志记录）
     */
    private String responseBody;
}
