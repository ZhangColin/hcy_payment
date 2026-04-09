package com.aieducenter.openapi.infrastructure.signature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("HmacSha256SignatureCalculator 测试")
class HmacSha256SignatureCalculatorTest {

    private HmacSha256SignatureCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new HmacSha256SignatureCalculator();
    }

    @Test
    @DisplayName("给定已知输入，计算结果应该为小写 hex 格式")
    void given_knownInput_when_calculate_then_returnsLowercaseHex() {
        String result = calculator.calculate("test string to sign", "secret key");

        assertThat(result).matches("[0-9a-f]+");
        assertThat(result).hasSize(64);
    }

    @Test
    @DisplayName("给定相同输入，计算结果应该一致")
    void given_sameInput_when_calculateTwice_then_sameResult() {
        String result1 = calculator.calculate("appId=test&nonce=abc", "mySecret");
        String result2 = calculator.calculate("appId=test&nonce=abc", "mySecret");

        assertThat(result1).isEqualTo(result2);
    }

    @Test
    @DisplayName("给定不同密钥，计算结果应该不同")
    void given_differentSecret_when_calculate_then_differentResult() {
        String result1 = calculator.calculate("same string", "secret1");
        String result2 = calculator.calculate("same string", "secret2");

        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    @DisplayName("给定不同待签名字符串，计算结果应该不同")
    void given_differentStringToSign_when_calculate_then_differentResult() {
        String result1 = calculator.calculate("string1", "sameSecret");
        String result2 = calculator.calculate("string2", "sameSecret");

        assertThat(result1).isNotEqualTo(result2);
    }
}
