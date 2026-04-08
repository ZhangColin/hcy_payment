package com.aieducenter.payment.domain.port.response;

import lombok.Builder;
import lombok.Data;

/**
 * 创建退款响应
 *
 * <p>支付网关返回的创建退款结果</p>
 */
@Data
@Builder
public class CreateRefundResponse {
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
     * 银行退款流水号
     */
    private String bankRefundNo;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionTime;
}
