package com.aieducenter.openapi.application;

import com.aieducenter.openapi.domain.aggregate.ApiKey;
import com.aieducenter.openapi.domain.enums.ApiKeyStatus;
import com.aieducenter.openapi.domain.error.OpenApiMessage;
import com.aieducenter.openapi.domain.repository.ApiKeyRepository;
import com.cartisan.core.exception.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApiKeyQueryAppService 测试")
class ApiKeyQueryAppServiceTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @InjectMocks
    private ApiKeyQueryAppService apiKeyQueryAppService;

    @Test
    @DisplayName("给定有效的活跃 API Key，查询时应该返回 ApiKey 聚合")
    void given_activeApiKey_when_findByApiKeyForVerification_then_returnsApiKey() throws Exception {
        ApiKey apiKey = createTestApiKey("APPKEY123", "secret123", "TestSystem", ApiKeyStatus.ACTIVE);
        when(apiKeyRepository.findByApiKey("APPKEY123")).thenReturn(Optional.of(apiKey));

        ApiKey result = apiKeyQueryAppService.findByApiKeyForVerification("APPKEY123");

        assertThat(result).isNotNull();
        assertThat(result.getApiKey()).isEqualTo("APPKEY123");
        assertThat(result.getApiSecret()).isEqualTo("secret123");
    }

    @Test
    @DisplayName("给定不存在的 API Key，查询时应该抛出 SIGNATURE_API_KEY_INVALID")
    void given_nonExistentApiKey_when_findByApiKeyForVerification_then_throwsException() {
        when(apiKeyRepository.findByApiKey("NOTEXIST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apiKeyQueryAppService.findByApiKeyForVerification("NOTEXIST"))
            .isInstanceOf(ApplicationException.class)
            .hasMessageContaining(OpenApiMessage.SIGNATURE_API_KEY_INVALID.message());
    }

    @Test
    @DisplayName("给定已禁用的 API Key，查询时应该抛出 SIGNATURE_API_KEY_INVALID")
    void given_disabledApiKey_when_findByApiKeyForVerification_then_throwsException() throws Exception {
        ApiKey disabledKey = createTestApiKey("DISABLEDKEY", "secret", "TestSystem", ApiKeyStatus.DISABLED);
        when(apiKeyRepository.findByApiKey("DISABLEDKEY")).thenReturn(Optional.of(disabledKey));

        assertThatThrownBy(() -> apiKeyQueryAppService.findByApiKeyForVerification("DISABLEDKEY"))
            .isInstanceOf(ApplicationException.class)
            .hasMessageContaining(OpenApiMessage.SIGNATURE_API_KEY_INVALID.message());
    }

    private ApiKey createTestApiKey(String apiKey, String apiSecret, String businessSystemName, ApiKeyStatus status) throws Exception {
        ApiKey key = new ApiKey(businessSystemName, "test", null);

        var apiKeyField = ApiKey.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(key, apiKey);

        var apiSecretField = ApiKey.class.getDeclaredField("apiSecret");
        apiSecretField.setAccessible(true);
        apiSecretField.set(key, apiSecret);

        if (status == ApiKeyStatus.DISABLED) {
            key.disable();
        }

        return key;
    }
}
