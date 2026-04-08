package com.aieducenter.account.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.aieducenter.account.infrastructure.security.BCryptAccountPasswordEncoderAdapter;

/**
 * AccountPasswordEncoderService 测试。
 */
class AccountPasswordEncoderServiceTest {

    private final AccountPasswordEncoderService accountPasswordEncoderService =
        new AccountPasswordEncoderService(new BCryptAccountPasswordEncoderAdapter());

    @Test
    void given_plain_password_when_encodePassword_then_password_is_encoded() {
        // When
        String encodedPassword = accountPasswordEncoderService.encodePassword("plainPassword123");

        // Then
        assertThat(encodedPassword).isNotNull();
        assertThat(encodedPassword).isNotEqualTo("plainPassword123");

        // 验证 BCrypt 格式（以 $2a$ 或 $2b$ 开头）
        assertThat(encodedPassword).matches("^\\$2[ab]\\$\\d+\\$[./A-Za-z0-9]{53}$");
    }

    @Test
    void given_same_password_encoded_twice_when_encodePassword_then_results_are_different() {
        // When
        String encoded1 = accountPasswordEncoderService.encodePassword("samePassword");
        String encoded2 = accountPasswordEncoderService.encodePassword("samePassword");

        // Then - BCrypt 每次生成不同的盐值，结果应该不同
        assertThat(encoded1).isNotEqualTo(encoded2);
    }

    @Test
    void given_correct_password_when_verifyPassword_then_return_true() {
        // Given
        String plainPassword = "myPassword123";
        String encodedPassword = accountPasswordEncoderService.encodePassword(plainPassword);

        // When & Then
        assertThat(accountPasswordEncoderService.verifyPassword(plainPassword, encodedPassword)).isTrue();
    }

    @Test
    void given_incorrect_password_when_verifyPassword_then_return_false() {
        // Given
        String encodedPassword = accountPasswordEncoderService.encodePassword("myPassword123");

        // When & Then
        assertThat(accountPasswordEncoderService.verifyPassword("wrongPassword", encodedPassword)).isFalse();
    }


    @Test
    void given_null_encoded_password_when_verifyPassword_then_return_false() {
        // When & Then
        assertThat(accountPasswordEncoderService.verifyPassword("password", null)).isFalse();
    }

    @Test
    void given_empty_password_when_verifyPassword_then_return_false() {
        // Given
        String encodedPassword = accountPasswordEncoderService.encodePassword("myPassword123");

        // When & Then
        assertThat(accountPasswordEncoderService.verifyPassword("", encodedPassword)).isFalse();
    }
}
