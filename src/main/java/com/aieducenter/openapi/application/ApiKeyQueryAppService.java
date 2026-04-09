package com.aieducenter.openapi.application;

import com.aieducenter.openapi.domain.aggregate.ApiKey;
import com.aieducenter.openapi.domain.enums.ApiKeyStatus;
import com.aieducenter.openapi.domain.error.OpenApiMessage;
import com.aieducenter.openapi.domain.repository.ApiKeyRepository;
import com.cartisan.core.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 签名验证专用的 ApiKey 查询服务
 *
 * <p>返回完整 ApiKey 聚合（包含 apiSecret），与 ApiKeyManagementAppService 区分</p>
 */
@Service
@RequiredArgsConstructor
public class ApiKeyQueryAppService {

    private final ApiKeyRepository apiKeyRepository;

    /**
     * 根据 apiKey 查询并验证状态
     *
     * @param apiKey API Key 字符串
     * @return 活跃的 ApiKey 聚合
     * @throws ApplicationException API Key 不存在或已禁用
     */
    @Transactional(readOnly = true)
    public ApiKey findByApiKeyForVerification(String apiKey) {
        return apiKeyRepository.findByApiKey(apiKey)
            .filter(key -> key.getStatus() == ApiKeyStatus.ACTIVE)
            .orElseThrow(() -> new ApplicationException(OpenApiMessage.SIGNATURE_API_KEY_INVALID));
    }
}
