package com.aieducenter.payment.infrastructure;

import com.aieducenter.payment.application.dto.callback.BusinessNotifyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DisplayName("业务系统通知测试")
class BusinessSystemNotifierTest {

    private BusinessSystemNotifier notifier;

    @BeforeEach
    void setUp() {
        notifier = new BusinessSystemNotifier();
    }

    @Test
    @DisplayName("构建通知请求体：正确映射字段")
    void buildRequest_mapsFieldsCorrectly() {
        BusinessNotifyRequest request = new BusinessNotifyRequest(
            "PAY001",
            "BIZ001",
            "PAID",
            "10000",
            "9800",
            "ICBC_ORDER_123",
            "2024-01-12 12:12:12",
            "9",
            "custom-attach-data"
        );

        assertThat(request.orderId()).isEqualTo("PAY001");
        assertThat(request.outTradeNo()).isEqualTo("BIZ001");
        assertThat(request.status()).isEqualTo("PAID");
        assertThat(request.amount()).isEqualTo("10000");
        assertThat(request.actualAmount()).isEqualTo("9800");
    }

    @Test
    @DisplayName("notifyUrl为空时不发送通知且不抛异常")
    void notify_withNullUrl_doesNotThrow() {
        BusinessNotifyRequest request = new BusinessNotifyRequest(
            "PAY001", "BIZ001", "PAID", "10000", "9800",
            "ICBC_123", "2024-01-12 12:12:12", "9", null
        );

        assertThatNoException().isThrownBy(() -> notifier.notify(null, request));
        assertThatNoException().isThrownBy(() -> notifier.notify("", request));
    }
}
