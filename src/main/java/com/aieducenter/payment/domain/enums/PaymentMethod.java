package com.aieducenter.payment.domain.enums;

import com.cartisan.core.domain.BaseEnum;
import com.cartisan.data.jpa.converter.BaseEnumConverter;
import jakarta.persistence.Converter;

public enum PaymentMethod implements BaseEnum<PaymentMethod> {
    WECHAT(9, "微信"),
    ALIPAY(10, "支付宝"),
    ICBC(1, "工商银行");

    private final Integer code;
    private final String name;

    PaymentMethod(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends BaseEnumConverter<PaymentMethod> {
        public JpaConverter() {
            super(PaymentMethod.class);
        }
    }
}
