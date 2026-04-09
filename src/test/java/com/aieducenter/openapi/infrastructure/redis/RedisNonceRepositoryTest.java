package com.aieducenter.openapi.infrastructure.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisNonceRepository 测试")
class RedisNonceRepositoryTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisNonceRepository nonceRepository;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        nonceRepository = new RedisNonceRepository(redisTemplate, 300);
    }

    @Test
    @DisplayName("给定新 Nonce，existsAndStore 应该返回 false 并存储")
    void given_newNonce_when_existsAndStore_then_returnsFalse() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
            .thenReturn(true);

        boolean result = nonceRepository.existsAndStore("new-nonce-123");

        assertThat(result).isFalse();
        verify(valueOperations).setIfAbsent(
            eq("openapi:nonce:new-nonce-123"),
            eq("1"),
            eq(Duration.ofSeconds(300))
        );
    }

    @Test
    @DisplayName("给定已存在的 Nonce，existsAndStore 应该返回 true")
    void given_existingNonce_when_existsAndStore_then_returnsTrue() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
            .thenReturn(false);

        boolean result = nonceRepository.existsAndStore("existing-nonce");

        assertThat(result).isTrue();
    }
}
