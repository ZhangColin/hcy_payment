package com.aieducenter.payment.application.dto.callback;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * 通知业务系统的请求体
 */
public record BusinessNotifyRequest(
    @JSONField(name = "orderId")
    String orderId,

    @JSONField(name = "outTradeNo")
    String outTradeNo,

    @JSONField(name = "status")
    String status,

    @JSONField(name = "amount")
    String amount,

    @JSONField(name = "actualAmount")
    String actualAmount,

    @JSONField(name = "bankOrderNo")
    String bankOrderNo,

    @JSONField(name = "payTime")
    String payTime,

    @JSONField(name = "payType")
    String payType,

    @JSONField(name = "attach")
    String attach
) {}
