package com.aieducenter.payment.application;

import com.aieducenter.payment.application.dto.command.CreatePaymentCommand;
import com.aieducenter.payment.application.dto.response.PaymentOrderResponse;
import com.aieducenter.payment.application.mapper.PaymentOrderMapper;
import com.aieducenter.payment.domain.aggregate.PaymentOrder;
import com.aieducenter.payment.domain.aggregate.PaymentLog;
import com.aieducenter.payment.domain.enums.PaymentStatus;
import com.aieducenter.payment.domain.port.PaymentGatewayPort;
import com.aieducenter.payment.domain.port.response.CreatePaymentResponse;
import com.aieducenter.payment.domain.repository.PaymentLogRepository;
import com.aieducenter.payment.domain.repository.PaymentOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 支付应用服务
 *
 * <p>负责支付相关的业务编排</p>
 */
@Service
@RequiredArgsConstructor
public class PaymentAppService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentLogRepository paymentLogRepository;

    /**
     * 创建支付订单
     *
     * @param command 创建支付命令
     * @param businessSystemName 业务系统名称（从 API Key 获取）
     * @return 支付订单响应
     */
    @Transactional
    public PaymentOrderResponse createPayment(CreatePaymentCommand command, String businessSystemName) {
        // 1. 创建支付订单
        PaymentOrder paymentOrder = new PaymentOrder(
            command.businessOrderNo(),
            businessSystemName,
            command.businessName(),
            command.amount(),
            command.subject(),
            command.body(),
            command.clientIp(),
            command.notifyUrl(),
            command.returnUrl(),
            command.attach(),
            command.expiredSeconds()
        );

        // 2. 检查业务订单号是否已存在
        if (paymentOrderRepository.existsByPaymentOrderNo(paymentOrder.getPaymentOrderNo())) {
            throw new IllegalArgumentException("支付订单号已存在");
        }

        // 3. 保存支付订单
        PaymentOrder saved = paymentOrderRepository.save(paymentOrder);

        // 4. 调用银行网关创建支付
        CreatePaymentResponse gatewayResponse = paymentGatewayPort.createPayment(paymentOrder);

        // 5. 记录调用日志
        PaymentLog log = new PaymentLog(
            paymentOrder.getPaymentOrderNo(),
            null, // refundOrderNo
            "PAYMENT_REQUEST",
            "ICBC",
            "qrcode/consumption",
            null, // requestUrl - not needed for logging
            gatewayResponse.requestParams(),
            gatewayResponse.responseBody(),
            200, // httpStatus
            gatewayResponse.returnCode(),
            gatewayResponse.returnMsg(),
            gatewayResponse.executionTime(),
            gatewayResponse.success(),
            gatewayResponse.success() ? null : "银行调用失败"
        );
        paymentLogRepository.save(log);

        // 6. 如果银行调用成功，更新支付订单状态
        // 注意：这里只是创建了支付订单，支付状态仍然是 PENDING
        // 实际的支付成功需要等待银行回调或主动查询

        return PaymentOrderMapper.convert(saved);
    }

    /**
     * 查询支付订单
     *
     * @param paymentOrderNo 支付订单号
     * @return 支付订单响应
     */
    @Transactional(readOnly = true)
    public PaymentOrderResponse getPayment(String paymentOrderNo) {
        PaymentOrder paymentOrder = paymentOrderRepository.findByPaymentOrderNo(paymentOrderNo)
            .orElseThrow(() -> new IllegalArgumentException("支付订单不存在"));

        // 如果订单已过期，标记为过期状态
        if (paymentOrder.isExpired() && paymentOrder.getStatus().equals(PaymentStatus.PENDING)) {
            paymentOrder.markAsExpired();
            paymentOrderRepository.save(paymentOrder);
        }

        return PaymentOrderMapper.convert(paymentOrder);
    }

    /**
     * 主动查询支付状态
     *
     * @param paymentOrderNo 支付订单号
     * @return 支付订单响应
     */
    @Transactional
    public PaymentOrderResponse queryPaymentStatus(String paymentOrderNo) {
        PaymentOrder paymentOrder = paymentOrderRepository.findByPaymentOrderNo(paymentOrderNo)
            .orElseThrow(() -> new IllegalArgumentException("支付订单不存在"));

        // 调用银行网关查询支付状态
        var queryResponse = paymentGatewayPort.queryPayment(
            paymentOrderNo,
            paymentOrder.getBankOrderNo()
        );

        // 记录查询日志
        PaymentLog log = new PaymentLog(
            paymentOrderNo,
            null,
            "PAYMENT_QUERY",
            "ICBC",
            "aggregatepay/b2c/online/orderqry",
            null,
            null,
            com.alibaba.fastjson2.JSON.toJSONString(queryResponse),
            200,
            queryResponse.returnCode(),
            queryResponse.returnMsg(),
            queryResponse.executionTime(),
            queryResponse.success(),
            null
        );
        paymentLogRepository.save(log);

        // 如果查询成功且支付状态变化，更新支付订单
        if (queryResponse.success() && queryResponse.paymentStatus() != null) {
            if (queryResponse.paymentStatus().equals(PaymentStatus.PAID) &&
                paymentOrder.getStatus().equals(PaymentStatus.PENDING)) {

                paymentOrder.markAsPaid(
                    queryResponse.bankOrderNo(),
                    queryResponse.thirdPartyOrderNo(),
                    queryResponse.paymentMethod()
                );
                paymentOrderRepository.save(paymentOrder);
            } else if (queryResponse.paymentStatus().equals(PaymentStatus.FAILED) &&
                       paymentOrder.getStatus().equals(PaymentStatus.PENDING)) {

                paymentOrder.markAsFailed("银行查询返回支付失败");
                paymentOrderRepository.save(paymentOrder);
            }
        }

        return PaymentOrderMapper.convert(paymentOrder);
    }
}
