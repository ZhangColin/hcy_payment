package com.aieducenter.openapi.infrastructure.redis;

import com.aieducenter.openapi.domain.port.NonceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis Nonce 存储实现
 */
@Component
public class RedisNonceRepository implements NonceRepository {

    private static final String KEY_PREFIX = "openapi:nonce:";

    private final StringRedisTemplate redisTemplate;
    private final long nonceTtlSeconds;

    public RedisNonceRepository(StringRedisTemplate redisTemplate,
                                @Value("${openapi.signature.nonce-ttl:300}") long nonceTtlSeconds) {
        this.redisTemplate = redisTemplate;
        this.nonceTtlSeconds = nonceTtlSeconds;
    }

    @Override
    public boolean existsAndStore(String nonce) {
        Boolean absent = redisTemplate.opsForValue()
            .setIfAbsent(KEY_PREFIX + nonce, "1", Duration.ofSeconds(nonceTtlSeconds));
        return !Boolean.TRUE.equals(absent);
    }
}
