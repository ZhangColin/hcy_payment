package com.aieducenter.openapi.web;

import com.aieducenter.openapi.application.ApiKeyQueryAppService;
import com.aieducenter.openapi.domain.aggregate.ApiKey;
import com.aieducenter.openapi.domain.enums.ApiKeyStatus;
import com.aieducenter.openapi.domain.service.SignatureService;
import com.aieducenter.openapi.web.CachingRequestBodyFilter.CachedBodyHttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SignatureVerificationInterceptor 测试")
class SignatureVerificationInterceptorTest {

    @Mock private SignatureService signatureService;
    @Mock private ApiKeyQueryAppService apiKeyQueryAppService;
    private OpenApiSignatureConfig config;
    private ObjectMapper objectMapper;

    private SignatureVerificationInterceptor interceptor;

    @BeforeEach
    void setUp() {
        config = new OpenApiSignatureConfig();
        config.setEnabled(true);
        config.setTimestampTolerance(300);
        objectMapper = new ObjectMapper();
        interceptor = new SignatureVerificationInterceptor(signatureService, apiKeyQueryAppService, config, objectMapper);
    }

    @Test
    @DisplayName("给定非 HandlerMethod，应该直接放行")
    void given_nonHandlerMethod_when_preHandle_then_pass() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = interceptor.preHandle(null, response, new Object());
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("给定无 @RequireSignature 注解的方法，应该直接放行")
    void given_noAnnotation_when_preHandle_then_pass() throws Exception {
        HandlerMethod handlerMethod = createHandlerMethod("methodWithoutAnnotation");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(
            new MockRequestWithHeaders().build(), response, handlerMethod
        );

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("给定缺少必要 Header 的请求，应该返回 401")
    void given_missingHeaders_when_preHandle_then_returns401() throws Exception {
        HandlerMethod handlerMethod = createHandlerMethod("methodWithAnnotation");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockRequestWithHeaders request = new MockRequestWithHeaders()
            .header("X-App-Id", "testApp");

        boolean result = interceptor.preHandle(request.build(), response, handlerMethod);
        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("给定完整有效请求，应该放行并设置 request attributes")
    void given_validRequest_when_preHandle_then_passWithAttributes() throws Exception {
        HandlerMethod handlerMethod = createHandlerMethod("methodWithAnnotation");
        MockHttpServletResponse response = new MockHttpServletResponse();

        ApiKey apiKey = createTestApiKey("testApp", "secret", "TestSystem");
        doNothing().when(signatureService).validateTimestamp(anyString(), anyLong());
        doNothing().when(signatureService).validateNonce(anyString());
        when(apiKeyQueryAppService.findByApiKeyForVerification("testApp")).thenReturn(apiKey);
        when(signatureService.calculateBodyDigest(any(byte[].class))).thenReturn("serverDigest");
        doNothing().when(signatureService).verify(any(), any(), any(), any(), any(), any(), any(), anyLong());

        MockRequestWithHeaders requestBuilder = new MockRequestWithHeaders()
            .header("X-App-Id", "testApp")
            .header("X-Timestamp", String.valueOf(System.currentTimeMillis() / 1000))
            .header("X-Nonce", "nonce-123")
            .header("X-Sign", "valid-sign")
            .header("X-Body-Digest", "serverDigest");

        var request = requestBuilder.build();
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isTrue();
        assertThat(request.getAttribute("verifiedAppId")).isEqualTo("testApp");
        assertThat(request.getAttribute("verifiedBusinessSystemName")).isEqualTo("TestSystem");
    }

    // --- Helper methods ---

    @RequireSignature
    public void methodWithAnnotation() {}

    public void methodWithoutAnnotation() {}

    private HandlerMethod createHandlerMethod(String methodName) throws NoSuchMethodException {
        Method method = this.getClass().getMethod(methodName);
        return new HandlerMethod(this, method);
    }

    private ApiKey createTestApiKey(String apiKeyStr, String apiSecret, String businessSystemName) throws Exception {
        ApiKey key = new ApiKey(businessSystemName, "test", null);
        setField(key, "apiKey", apiKeyStr);
        setField(key, "apiSecret", apiSecret);
        return key;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Helper to build mock requests with headers.
     * Uses CachedBodyHttpServletRequest for body caching.
     */
    static class MockRequestWithHeaders {
        private final jakarta.servlet.http.HttpServletRequest mockRequest =
            new org.springframework.mock.web.MockHttpServletRequest();

        MockRequestWithHeaders header(String name, String value) {
            ((org.springframework.mock.web.MockHttpServletRequest) mockRequest).addHeader(name, value);
            return this;
        }

        CachedBodyHttpServletRequest build() {
            return new CachedBodyHttpServletRequest(
                (org.springframework.mock.web.MockHttpServletRequest) mockRequest,
                new byte[0]
            );
        }
    }
}
