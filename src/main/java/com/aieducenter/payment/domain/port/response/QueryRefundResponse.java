package com.aieducenter.payment.domain.port.response;

import com.aieducenter.payment.domain.enums.RefundStatus;
import lombok.Builder;
import lombok.Data;

/**
 * 查询退款响应
 *
 * <p>支付网关返回的退款状态查询结果</p>
 */
@Data
@Builder
public class QueryRefundResponse {
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
     * 退款状态
     */
    private RefundStatus refundStatus;

    /**
     * 退款金额（分）
     */
    private Long refundAmount;

    /**
     * 实际退款金额（分）
     */
    private Long realRefundAmount;

    /**
     * 退款时间
     */
    private String refundTime;

    /**
     * 银行退款流水号
     */
    private String bankRefundNo;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionTime;
}
