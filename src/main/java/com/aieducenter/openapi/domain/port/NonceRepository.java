package com.aieducenter.openapi.domain.port;

/**
 * Nonce 存储端口（防重放）
 */
public interface NonceRepository {
    /**
     * 检查 Nonce 是否已存在，不存在则存储
     * TTL 由实现类通过构造注入从配置获取
     *
     * @return true = 已存在（重复请求），false = 不存在（已存储）
     */
    boolean existsAndStore(String nonce);
}