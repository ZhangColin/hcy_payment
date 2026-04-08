package com.aieducenter.payment.domain.repository;

import com.aieducenter.payment.domain.aggregate.PaymentOrder;
import com.cartisan.data.jpa.repository.BaseRepository;

/**
 * 支付订单仓储接口
 *
 * <p>领域层定义，基础设施层提供实现</p>
 */
public interface PaymentOrderRepository extends BaseRepository<PaymentOrder, Long> {
    /**
     * 根据支付订单号查询
     *
     * @param paymentOrderNo 支付订单号
     * @return 支付订单
     */
    java.util.Optional<PaymentOrder> findByPaymentOrderNo(String paymentOrderNo);

    /**
     * 检查支付订单号是否存在
     *
     * @param paymentOrderNo 支付订单号
     * @return 是否存在
     */
    boolean existsByPaymentOrderNo(String paymentOrderNo);
}
