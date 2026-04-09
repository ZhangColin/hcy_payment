package com.aieducenter.payment.domain.aggregate;


import com.aieducenter.payment.domain.enums.PaymentMethod;
import com.aieducenter.payment.domain.enums.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PaymentOrder 聚合根测试
 *
 * <p>测试支付订单的业务逻辑和状态转换</p>
 */
@DisplayName("PaymentOrder 聚合根测试")
class PaymentOrderTest {

    @Test
    @DisplayName("给定有效输入，创建支付订单时应该成功")
    void given_valid_input_when_createPaymentOrder_then_success() {
        // When
        PaymentOrder order = new PaymentOrder(
            "ORDER001",
            "TestSystem",
            "课程购买",
            10000L,
            "Python课程",
            "Python编程课程",
            "127.0.0.1",
            "https://example.com/notify",
            "https://example.com/return",
            null,
            null
        );

        // Then
        assertThat(order.getBusinessOrderNo()).isEqualTo("ORDER001");
        assertThat(order.getBusinessSystemName()).isEqualTo("TestSystem");
        assertThat(order.getBusinessName()).isEqualTo("课程购买");
        assertThat(order.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(order.getAmount()).isEqualTo(10000L);
        assertThat(order.getCurrency()).isEqualTo("CNY");
        assertThat(order.getSubject()).isEqualTo("Python课程");
        assertThat(order.getBody()).isEqualTo("Python编程课程");
        assertThat(order.getClientIp()).isEqualTo("127.0.0.1");
        assertThat(order.getExpiredAt()).isNotNull();
    }

    @Test
    @DisplayName("给定待支付订单，标记为已支付时应该更新状态和时间戳")
    void given_pendingOrder_when_markAsPaid_then_statusUpdated() {
        // Given
        PaymentOrder order = new PaymentOrder(
            "ORDER001", "TestSystem", "课程购买",
            10000L, "Python", "Course", "127.0.0.1",
            "https://example.com/notify", "https://example.com/return", null, null
        );

        // When
        order.markAsPaid("ICBC123", "WX123", PaymentMethod.WECHAT, 10000L);

        // Then
        assertThat(order.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(order.getPaidAt()).isNotNull();
        assertThat(order.getBankOrderNo()).isEqualTo("ICBC123");
        assertThat(order.getThirdPartyOrderNo()).isEqualTo("WX123");
        assertThat(order.getPaymentMethod()).isEqualTo(PaymentMethod.WECHAT);
    }

    @Test
    @DisplayName("给定已支付订单，再次标记为已支付时应该抛出异常")
    void given_paidOrder_when_markAsPaid_then_throwsException() {
        // Given
        PaymentOrder order = new PaymentOrder(
            "ORDER001", "TestSystem", "课程购买",
            10000L, "Python", "Course", "127.0.0.1",
            "https://example.com/notify", "https://example.com/return", null, null
        );
        order.markAsPaid("ICBC123", "WX123", PaymentMethod.WECHAT, 10000L);

        // When & Then
        assertThatThrownBy(() -> order.markAsPaid("ICBC456", "WX456", PaymentMethod.ALIPAY, 5000L))
            .isInstanceOf(com.cartisan.core.exception.DomainException.class)
            .hasMessageContaining("支付订单不是待支付状态");
    }

    @Test
    @DisplayName("给定待支付订单，标记为失败时应该更新状态和时间戳")
    void given_pendingOrder_when_markAsFailed_then_statusUpdated() {
        // Given
        PaymentOrder order = new PaymentOrder(
            "ORDER001", "TestSystem", "课程购买",
            10000L, "Python", "Course", "127.0.0.1",
            "https://example.com/notify", "https://example.com/return", null, null
        );

        // When
        order.markAsFailed("余额不足");

        // Then
        assertThat(order.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(order.getFailedAt()).isNotNull();
    }

    @Test
    @DisplayName("给定待支付订单，取消时应该更新状态和时间戳")
    void given_pendingOrder_when_cancel_then_statusUpdated() {
        // Given
        PaymentOrder order = new PaymentOrder(
            "ORDER001", "TestSystem", "课程购买",
            10000L, "Python", "Course", "127.0.0.1",
            "https://example.com/notify", "https://example.com/return", null, null
        );

        // When
        order.cancel();

        // Then
        assertThat(order.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(order.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("给定待支付订单，标记为过期时应该更新状态")
    void given_pendingOrder_when_markAsExpired_then_statusUpdated() {
        // Given
        PaymentOrder order = new PaymentOrder(
            "ORDER001", "TestSystem", "课程购买",
            10000L, "Python", "Course", "127.0.0.1",
            "https://example.com/notify", "https://example.com/return", null, null
        );

        // When
        order.markAsExpired();

        // Then
        assertThat(order.getStatus()).isEqualTo(PaymentStatus.EXPIRED);
    }

    @Test
    @DisplayName("给定过期订单，检查是否过期时应该返回true")
    void given_expiredOrder_when_isExpired_then_returnsTrue() {
        // Given
        PaymentOrder order = new PaymentOrder(
            "ORDER001", "TestSystem", "课程购买",
            10000L, "Python", "Course", "127.0.0.1",
            "https://example.com/notify", "https://example.com/return", null,
            1L // 1 秒后过期
        );

        // When
        // 等待超过过期时间
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean result = order.isExpired();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("给定未过期订单，检查是否过期时应该返回false")
    void given_notExpiredOrder_when_isExpired_then_returnsFalse() {
        // Given
        PaymentOrder order = new PaymentOrder(
            "ORDER001", "TestSystem", "课程购买",
            10000L, "Python", "Course", "127.0.0.1",
            "https://example.com/notify", "https://example.com/return", null,
            3600L // 1 小时后过期
        );

        // When
        boolean result = order.isExpired();

        // Then
        assertThat(result).isFalse();
    }
}
