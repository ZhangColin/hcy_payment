package com.aieducenter.security.application.dto.response;

import com.aieducenter.security.domain.enums.ApiKeyStatus;

import java.time.LocalDateTime;

/**
 * API Key 响应
 *
 * <p>创建时包含 apiSecret（仅此一次展示），查询时不返回</p>
 */
public record ApiKeyResponse(
    Long id,
    String apiKey,
    String apiSecret,
    String businessSystemName,
    ApiKeyStatus status,
    String permissions,
    String description,
    LocalDateTime createdAt
) {
}
