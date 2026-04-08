package com.aieducenter.account.domain.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.aieducenter.account.domain.error.UserError;
import com.cartisan.core.exception.DomainException;

/**
 * User 聚合根测试。
 */
class UserTest {

    // ========== 创建用户 ==========

    @Test
    void given_valid_fields_when_createUser_then_success() {
        // When
        User user = new User("john_doe", "encodedPassword123", "John Doe");

        // Then
        assertThat(user.getUsername()).isEqualTo("john_doe");
        assertThat(user.getNickname()).isEqualTo("John Doe");
        assertThat(user.getPassword()).isEqualTo("encodedPassword123");
    }

    @Test
    void given_blank_nickname_when_createUser_then_nickname_is_username() {
        // When
        User user = new User("john_doe", "encodedPassword123", "");

        // Then
        assertThat(user.getNickname()).isEqualTo("john_doe");
    }

    @Test
    void given_null_nickname_when_createUser_then_nickname_is_username() {
        // When
        User user = new User("john_doe", "encodedPassword123", null);

        // Then
        assertThat(user.getNickname()).isEqualTo("john_doe");
    }

    @Test
    void given_spaces_nickname_when_createUser_then_nickname_is_username() {
        // When
        User user = new User("john_doe", "encodedPassword123", "   ");

        // Then
        assertThat(user.getNickname()).isEqualTo("john_doe");
    }

    @Test
    void given_null_password_when_createUser_then_throw_password_weak() {
        // When & Then
        assertThatThrownBy(() -> new User("john_doe", null, "John Doe"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PASSWORD_WEAK);
    }

    // ========== 用户名格式验证 ==========

    @Test
    void given_valid_username_when_createUser_then_success() {
        // When & Then - 字母开头
        assertThat(new User("abc", "encodedPassword123", null).getUsername()).isEqualTo("abc");

        // When & Then - 字母开头，包含数字和下划线
        assertThat(new User("user_123", "encodedPassword123", null).getUsername()).isEqualTo("user_123");

        // When & Then - 最大长度 20
        String maxUsername = "a" + "_".repeat(18) + "b";
        assertThat(new User(maxUsername, "encodedPassword123", null).getUsername()).hasSize(20);
    }

    @Test
    void given_username_starts_with_number_when_createUser_then_throw_username_invalid() {
        // When & Then
        assertThatThrownBy(() -> new User("123invalid", "encodedPassword123", null))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    @Test
    void given_username_starts_with_underscore_when_createUser_then_throw_username_invalid() {
        // When & Then
        assertThatThrownBy(() -> new User("_invalid", "encodedPassword123", null))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    @Test
    void given_username_too_short_when_createUser_then_throw_username_invalid() {
        // When & Then - 2 字符
        assertThatThrownBy(() -> new User("ab", "encodedPassword123", null))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);

        // When & Then - 1 字符
        assertThatThrownBy(() -> new User("a", "encodedPassword123", null))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    @Test
    void given_username_too_long_when_createUser_then_throw_username_invalid() {
        // When & Then - 21 字符
        assertThatThrownBy(() -> new User("a".repeat(21), "encodedPassword123", null))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    @Test
    void given_username_contains_invalid_chars_when_createUser_then_throw_username_invalid() {
        // When & Then - 包含特殊字符
        assertThatThrownBy(() -> new User("user@name", "encodedPassword123", null))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    // ========== 修改用户名 ==========

    @Test
    void given_valid_new_username_when_updateUsername_then_success() {
        // Given
        User user = new User("john_doe", "encodedPassword123", null);

        // When
        user.updateUsername("jane_doe");

        // Then
        assertThat(user.getUsername()).isEqualTo("jane_doe");
    }

    @Test
    void given_invalid_new_username_when_updateUsername_then_throw_username_invalid() {
        // Given
        User user = new User("john_doe", "encodedPassword123", null);

        // When & Then
        assertThatThrownBy(() -> user.updateUsername("123invalid"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    // ========== 修改昵称 ==========

    @Test
    void given_valid_new_nickname_when_updateNickname_then_success() {
        // Given
        User user = new User("john_doe", "encodedPassword123", "Old Nickname");

        // When
        user.updateNickname("New Nickname");

        // Then
        assertThat(user.getNickname()).isEqualTo("New Nickname");
    }

    @Test
    void given_blank_new_nickname_when_updateNickname_then_not_update() {
        // Given
        User user = new User("john_doe", "encodedPassword123", "Old Nickname");

        // When
        user.updateNickname("");

        // Then - 昵称不应被更新
        assertThat(user.getNickname()).isEqualTo("Old Nickname");
    }

    @Test
    void given_null_new_nickname_when_updateNickname_then_not_update() {
        // Given
        User user = new User("john_doe", "encodedPassword123", "Old Nickname");

        // When
        user.updateNickname(null);

        // Then - 昵称不应被更新
        assertThat(user.getNickname()).isEqualTo("Old Nickname");
    }

    // ========== 修改头像 ==========

    @Test
    void given_valid_avatar_when_updateAvatar_then_success() {
        // Given
        User user = new User("john_doe", "encodedPassword123", null);

        // When
        user.setAvatar("https://example.com/avatar.jpg");

        // Then
        assertThat(user.getAvatar()).isEqualTo("https://example.com/avatar.jpg");
    }

    @Test
    void given_null_avatar_when_updateAvatar_then_cleared() {
        // Given
        User user = new User("john_doe", "encodedPassword123", "https://example.com/avatar.jpg");

        // When
        user.setAvatar(null);

        // Then
        assertThat(user.getAvatar()).isNull();
    }

    // ========== 修改密码 ==========

    @Test
    void given_correct_old_password_and_valid_new_password_when_updatePassword_then_success() {
        // Given
        User user = new User("john_doe", "oldEncodedPassword", null);

        // When
        user.updatePassword("oldEncodedPassword", "newEncodedPassword");

        // Then
        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
    }

    @Test
    void given_incorrect_old_password_when_updatePassword_then_throw_password_incorrect() {
        // Given
        User user = new User("john_doe", "oldEncodedPassword", null);

        // When & Then
        assertThatThrownBy(() -> user.updatePassword("wrongPassword", "newEncodedPassword"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PASSWORD_INCORRECT);
    }

    @Test
    void given_null_new_password_when_updatePassword_then_throw_password_weak() {
        // Given
        User user = new User("john_doe", "oldEncodedPassword", null);

        // When & Then
        assertThatThrownBy(() -> user.updatePassword("oldEncodedPassword", null))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PASSWORD_WEAK);
    }

    // ========== 重置密码 ==========

    @Test
    void given_valid_encoded_password_when_resetPassword_then_success() {
        // Given
        User user = User.restore(1L, "john_doe", null, null, "oldEncodedPassword", "John Doe", null);

        // When
        user.resetPassword("newEncodedPassword");

        // Then
        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
    }

    @Test
    void given_null_password_when_resetPassword_then_throw_password_weak() {
        // Given
        User user = User.restore(1L, "john_doe", null, null, "oldEncodedPassword", "John Doe", null);

        // When & Then
        assertThatThrownBy(() -> user.resetPassword(null))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PASSWORD_WEAK);
    }

    // ========== Email 和 PhoneNumber（直接 setter）==========

    @Test
    void given_valid_email_when_setEmail_then_success() {
        // Given
        User user = new User("john_doe", "encodedPassword123", null);

        // When
        user.setEmail("john@example.com");

        // Then
        assertThat(user.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void given_null_email_when_setEmail_then_cleared() {
        // Given
        User user = new User("john_doe", "encodedPassword123", "john@example.com");

        // When
        user.setEmail(null);

        // Then
        assertThat(user.getEmail()).isNull();
    }

    @Test
    void given_valid_phone_when_setPhoneNumber_then_success() {
        // Given
        User user = new User("john_doe", "encodedPassword123", null);

        // When
        user.setPhoneNumber("13812345678");

        // Then
        assertThat(user.getPhoneNumber()).isEqualTo("13812345678");
    }

    @Test
    void given_null_phone_when_setPhoneNumber_then_cleared() {
        // Given
        User user = new User("john_doe", "encodedPassword123", "13812345678");

        // When
        user.setPhoneNumber(null);

        // Then
        assertThat(user.getPhoneNumber()).isNull();
    }

    // ========== register 静态工厂方法 ==========

    @Test
    void given_username_and_password_only_when_register_then_success() {
        // When
        User user = User.register("john_doe", "encodedPassword123", null, null, null);

        // Then
        assertThat(user.getUsername()).isEqualTo("john_doe");
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPhoneNumber()).isNull();
    }

    @Test
    void given_email_provided_when_register_then_email_set() {
        // When
        User user = User.register("john_doe", "encodedPassword123", "John Doe", "john@example.com", null);

        // Then
        assertThat(user.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void given_phone_provided_when_register_then_phone_set() {
        // When
        User user = User.register("john_doe", "encodedPassword123", "John Doe", null, "13812345678");

        // Then
        assertThat(user.getPhoneNumber()).isEqualTo("13812345678");
    }

    @Test
    void given_both_email_and_phone_when_register_then_both_set() {
        // When
        User user = User.register("john_doe", "encodedPassword123", "John Doe", "john@example.com", "13812345678");

        // Then
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getPhoneNumber()).isEqualTo("13812345678");
    }

    // ========== restore 静态工厂方法 ==========

    @Test
    void given_valid_fields_when_restore_then_success() {
        // When
        User user = User.restore(1L, "john_doe", "john@example.com", "13812345678",
                                  "encodedPassword", "John Doe", "https://example.com/avatar.jpg");

        // Then
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("john_doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getPhoneNumber()).isEqualTo("13812345678");
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        assertThat(user.getNickname()).isEqualTo("John Doe");
        assertThat(user.getAvatar()).isEqualTo("https://example.com/avatar.jpg");
    }
}
