package com.aieducenter.openapi.domain.service;

import com.aieducenter.openapi.domain.aggregate.ApiKey;
import com.aieducenter.openapi.domain.error.OpenApiMessage;
import com.aieducenter.openapi.domain.port.NonceRepository;
import com.aieducenter.openapi.domain.port.SignatureCalculator;
import com.cartisan.core.exception.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SignatureService 签名验证领域服务测试")
class SignatureServiceTest {

    @Mock
    private SignatureCalculator signatureCalculator;

    @Mock
    private NonceRepository nonceRepository;

    private SignatureService signatureService;

    private ApiKey testApiKey;

    @BeforeEach
    void setUp() throws Exception {
        signatureService = new SignatureService(signatureCalculator, nonceRepository);
        testApiKey = createTestApiKey("TESTAPPKEY12345678901234567890", "testSecret", "TestSystem");
    }

    private ApiKey createTestApiKey(String apiKey, String apiSecret, String businessSystemName) throws Exception {
        ApiKey key = new ApiKey(businessSystemName, "test", null);

        var apiKeyField = ApiKey.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(key, apiKey);

        var apiSecretField = ApiKey.class.getDeclaredField("apiSecret");
        apiSecretField.setAccessible(true);
        apiSecretField.set(key, apiSecret);

        return key;
    }

    @Test
    @DisplayName("给定有效参数，计算签名时应该正确排序拼接并调用 SignatureCalculator")
    void given_validParams_when_calculateSign_then_correctlySortsAndConcatenates() {
        when(signatureCalculator.calculate(anyString(), eq("testSecret"))).thenReturn("calculated_sign");

        String result = signatureService.calculateSign(
            "TESTAPPKEY12345678901234567890",
            "bodyDigest123",
            "nonce-abc",
            "1710000000",
            Map.of("name", "zhangsan"),
            "testSecret"
        );

        assertThat(result).isEqualTo("calculated_sign");
        verify(signatureCalculator).calculate(
            eq("appId=TESTAPPKEY12345678901234567890&bodyDigest=bodyDigest123&name=zhangsan&nonce=nonce-abc&timestamp=1710000000"),
            eq("testSecret")
        );
    }

    @Test
    @DisplayName("给定无 query 参数，计算签名时应该只包含系统参数和 bodyDigest")
    void given_noQueryParams_when_calculateSign_then_onlySystemParams() {
        when(signatureCalculator.calculate(anyString(), eq("secret"))).thenReturn("sign");

        signatureService.calculateSign("appId", "digest", "nonce", "1234", Collections.emptyMap(), "secret");

        verify(signatureCalculator).calculate(
            eq("appId=appId&bodyDigest=digest&nonce=nonce&timestamp=1234"),
            eq("secret")
        );
    }

    @Test
    @DisplayName("给定有效请求，验证签名时应该通过")
    void given_validRequest_when_verify_then_pass() {
        when(signatureCalculator.calculate(anyString(), eq("testSecret"))).thenReturn("expected_sign");

        assertThatCode(() -> signatureService.verify(
            testApiKey,
            String.valueOf(System.currentTimeMillis() / 1000),
            "nonce-123",
            "expected_sign",
            "bodyDigest123",
            "bodyDigest123",
            Collections.emptyMap(),
            300
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("给定过期时间戳，validateTimestamp 应该抛出 TIMESTAMP_EXPIRED")
    void given_expiredTimestamp_when_validateTimestamp_then_throwTimestampExpired() {
        long expiredTimestamp = System.currentTimeMillis() / 1000 - 600;

        assertThatThrownBy(() -> signatureService.validateTimestamp(String.valueOf(expiredTimestamp), 300))
        .isInstanceOf(ApplicationException.class)
        .hasMessageContaining(OpenApiMessage.SIGNATURE_TIMESTAMP_EXPIRED.message());
    }

    @Test
    @DisplayName("给定有效时间戳，validateTimestamp 应该通过")
    void given_validTimestamp_when_validateTimestamp_then_pass() {
        long currentTimestamp = System.currentTimeMillis() / 1000;

        assertThatCode(() -> signatureService.validateTimestamp(String.valueOf(currentTimestamp), 300))
        .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("给定重复 Nonce，validateNonce 应该抛出 NONCE_DUPLICATED")
    void given_duplicatedNonce_when_validateNonce_then_throwNonceDuplicated() {
        when(nonceRepository.existsAndStore("nonce-dup")).thenReturn(true);

        assertThatThrownBy(() -> signatureService.validateNonce("nonce-dup"))
        .isInstanceOf(ApplicationException.class)
        .hasMessageContaining(OpenApiMessage.SIGNATURE_NONCE_DUPLICATED.message());
    }

    @Test
    @DisplayName("给定新 Nonce，validateNonce 应该通过")
    void given_newNonce_when_validateNonce_then_pass() {
        when(nonceRepository.existsAndStore("nonce-new")).thenReturn(false);

        assertThatCode(() -> signatureService.validateNonce("nonce-new"))
        .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("给定不匹配的 bodyDigest，验证签名时应该抛出 BODY_DIGEST_MISMATCH")
    void given_mismatchedBodyDigest_when_verify_then_throwBodyDigestMismatch() {
        assertThatThrownBy(() -> signatureService.verify(
            testApiKey,
            String.valueOf(System.currentTimeMillis() / 1000),
            "nonce-123",
            "sign",
            "clientDigest",
            "serverDigest",
            Collections.emptyMap(),
            300
        ))
        .isInstanceOf(ApplicationException.class)
        .hasMessageContaining(OpenApiMessage.SIGNATURE_BODY_DIGEST_MISMATCH.message());
    }

    @Test
    @DisplayName("给定不匹配的签名，验证签名时应该抛出 SIGNATURE_MISMATCH")
    void given_mismatchedSignature_when_verify_then_throwSignatureMismatch() {
        when(signatureCalculator.calculate(anyString(), anyString())).thenReturn("server_sign");

        assertThatThrownBy(() -> signatureService.verify(
            testApiKey,
            String.valueOf(System.currentTimeMillis() / 1000),
            "nonce-123",
            "client_sign",
            "sameDigest",
            "sameDigest",
            Collections.emptyMap(),
            300
        ))
        .isInstanceOf(ApplicationException.class)
        .hasMessageContaining(OpenApiMessage.SIGNATURE_MISMATCH.message());
    }
}
