package com.aieducenter.payment.domain.repository;

import com.aieducenter.payment.domain.aggregate.RefundOrder;
import com.cartisan.data.jpa.repository.BaseRepository;

/**
 * 退款订单仓储接口
 *
 * <p>领域层定义，基础设施层提供实现</p>
 */
public interface RefundOrderRepository extends BaseRepository<RefundOrder, Long> {
    /**
     * 根据退款订单号查询
     *
     * @param refundOrderNo 退款订单号
     * @return 退款订单
     */
    java.util.Optional<RefundOrder> findByRefundOrderNo(String refundOrderNo);

    /**
     * 检查退款订单号是否存在
     *
     * @param refundOrderNo 退款订单号
     * @return 是否存在
     */
    boolean existsByRefundOrderNo(String refundOrderNo);
}
