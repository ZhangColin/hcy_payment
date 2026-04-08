package com.aieducenter.payment.application.mapper;

import com.aieducenter.payment.application.dto.response.PaymentOrderResponse;
import com.aieducenter.payment.domain.aggregate.PaymentOrder;
import org.mapstruct.Mapper;

/**
 * 支付订单映射器
 *
 * <p>使用 MapStruct 进行领域对象与 DTO 之间的转换</p>
 */
@Mapper(componentModel = "spring")
public interface PaymentOrderMapper {

    /**
     * 转换为响应 DTO
     *
     * @param paymentOrder 支付订单聚合根
     * @return 响应 DTO
     */
    PaymentOrderResponse toResponse(PaymentOrder paymentOrder);

    /**
     * 静态便捷方法
     *
     * @param paymentOrder 支付订单聚合根
     * @return 响应 DTO
     */
    static PaymentOrderResponse convert(PaymentOrder paymentOrder) {
        return new PaymentOrderResponse(
            paymentOrder.getId(),
            paymentOrder.getBusinessOrderNo(),
            paymentOrder.getPaymentOrderNo(),
            paymentOrder.getBusinessSystemName(),
            paymentOrder.getBusinessType(),
            paymentOrder.getStatus(),
            paymentOrder.getAmount(),
            paymentOrder.getCurrency(),
            paymentOrder.getSubject(),
            paymentOrder.getBody(),
            paymentOrder.getPaymentMethod(),
            null, // qrCodeUrl - not stored in aggregate
            paymentOrder.getClientIp(),
            paymentOrder.getCreatedAt(),
            paymentOrder.getExpiredAt(),
            paymentOrder.getPaidAt(),
            paymentOrder.getBankOrderNo(),
            paymentOrder.getThirdPartyOrderNo()
        );
    }
}
