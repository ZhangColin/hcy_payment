package com.aieducenter.openapi.domain.repository;

import com.aieducenter.openapi.domain.aggregate.ApiKey;
import com.cartisan.data.jpa.repository.BaseRepository;

import java.util.Optional;

/**
 * API Key 仓储接口
 */
public interface ApiKeyRepository extends BaseRepository<ApiKey, Long> {
    Optional<ApiKey> findByApiKey(String apiKey);
    boolean existsByApiKey(String apiKey);
}
