package com.aieducenter.openapi.application;

import com.aieducenter.openapi.application.dto.command.CreateApiKeyCommand;
import com.aieducenter.openapi.application.dto.response.ApiKeyResponse;
import com.aieducenter.openapi.domain.aggregate.ApiKey;
import com.aieducenter.openapi.domain.enums.ApiKeyStatus;
import com.aieducenter.openapi.domain.error.OpenApiMessage;
import com.aieducenter.openapi.domain.repository.ApiKeyRepository;
import com.cartisan.core.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * API Key 管理应用服务
 */
@Service
@RequiredArgsConstructor
public class ApiKeyManagementAppService {

    private final ApiKeyRepository apiKeyRepository;

    /**
     * 创建 API Key
     */
    @Transactional
    public ApiKeyResponse createApiKey(CreateApiKeyCommand command) {
        ApiKey apiKey = new ApiKey(
            command.businessSystemName(),
            command.description(),
            command.permissions()
        );

        ApiKey saved = apiKeyRepository.save(apiKey);

        return new ApiKeyResponse(
            saved.getId(),
            saved.getApiKey(),
            saved.getApiSecret(), // 仅创建时返回
            saved.getBusinessSystemName(),
            saved.getStatus(),
            saved.getPermissions(),
            saved.getDescription(),
            saved.getCreatedAt()
        );
    }

    /**
     * 查询 API Key（不返回 secret）
     */
    @Transactional(readOnly = true)
    public ApiKeyResponse getApiKey(Long id) {
        ApiKey apiKey = apiKeyRepository.findById(id)
            .orElseThrow(() -> new ApplicationException(OpenApiMessage.API_KEY_NOT_FOUND));

        return new ApiKeyResponse(
            apiKey.getId(),
            apiKey.getApiKey(),
            null, // 查询时不返回 secret
            apiKey.getBusinessSystemName(),
            apiKey.getStatus(),
            apiKey.getPermissions(),
            apiKey.getDescription(),
            apiKey.getCreatedAt()
        );
    }

    /**
     * 通过 apiKey 字符串查询
     */
    @Transactional(readOnly = true)
    public ApiKeyResponse getApiKeyByKey(String apiKeyStr) {
        ApiKey apiKey = apiKeyRepository.findByApiKey(apiKeyStr)
            .orElseThrow(() -> new ApplicationException(OpenApiMessage.API_KEY_NOT_FOUND));

        return new ApiKeyResponse(
            apiKey.getId(),
            apiKey.getApiKey(),
            null,
            apiKey.getBusinessSystemName(),
            apiKey.getStatus(),
            apiKey.getPermissions(),
            apiKey.getDescription(),
            apiKey.getCreatedAt()
        );
    }

    /**
     * 认证 API Key 并返回业务系统名称
     *
     * @param apiKeyStr API Key 字符串
     * @return 业务系统名称
     */
    @Transactional(readOnly = true)
    public String authenticate(String apiKeyStr) {
        ApiKey apiKey = apiKeyRepository.findByApiKey(apiKeyStr)
            .orElseThrow(() -> new ApplicationException(OpenApiMessage.API_KEY_NOT_FOUND));

        if (apiKey.getStatus() != ApiKeyStatus.ACTIVE) {
            throw new ApplicationException(OpenApiMessage.API_KEY_DISABLED);
        }

        return apiKey.getBusinessSystemName();
    }

    /**
     * 禁用 API Key
     */
    @Transactional
    public void disableApiKey(Long id) {
        ApiKey apiKey = apiKeyRepository.findById(id)
            .orElseThrow(() -> new ApplicationException(OpenApiMessage.API_KEY_NOT_FOUND));
        apiKey.disable();
        apiKeyRepository.save(apiKey);
    }

    /**
     * 启用 API Key
     */
    @Transactional
    public void enableApiKey(Long id) {
        ApiKey apiKey = apiKeyRepository.findById(id)
            .orElseThrow(() -> new ApplicationException(OpenApiMessage.API_KEY_NOT_FOUND));
        apiKey.enable();
        apiKeyRepository.save(apiKey);
    }
}
