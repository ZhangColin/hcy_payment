package com.aieducenter.payment.domain.enums;

import com.cartisan.core.domain.BaseEnum;
import com.cartisan.data.jpa.converter.BaseEnumConverter;
import jakarta.persistence.Converter;

public enum BusinessType implements BaseEnum<BusinessType> {
    EDUCATION(1, "教育"),
    ECOMMERCE(2, "电商"),
    ENTERTAINMENT(3, "娱乐"),
    OTHER(99, "其他");

    private final Integer code;
    private final String name;

    BusinessType(Integer code, String name) {
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
    public static class JpaConverter extends BaseEnumConverter<BusinessType> {
        public JpaConverter() {
            super(BusinessType.class);
        }
    }
}
