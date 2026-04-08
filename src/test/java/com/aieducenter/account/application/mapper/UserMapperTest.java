package com.aieducenter.account.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aieducenter.account.application.dto.response.UserResponse;
import com.aieducenter.account.domain.aggregate.User;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapperImpl();
    }

    @Test
    void given_user_with_all_fields_when_convert_then_responseCorrect() {
        // Given
        User user = User.register("john_doe", "encoded", "John", "john@example.com", "13812345678");

        // When
        UserResponse response = userMapper.convert(user);

        // Then
        assertThat(response.username()).isEqualTo("john_doe");
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.phoneNumber()).isEqualTo("13812345678");
    }

    @Test
    void given_user_with_null_fields_when_convert_then_responseCorrect() {
        // Given
        User user = User.register("john_doe", "encoded", "John", null, null);

        // When
        UserResponse response = userMapper.convert(user);

        // Then
        assertThat(response.username()).isEqualTo("john_doe");
        assertThat(response.email()).isNull();
        assertThat(response.phoneNumber()).isNull();
    }

    @Test
    void given_user_list_when_convertList_then_allConverted() {
        // Given
        var users = java.util.List.of(
            User.register("user1", "encoded", "User 1", null, null),
            User.register("user2", "encoded", "User 2", "user2@example.com", "13812345678")
        );

        // When
        var responses = userMapper.convertList(users);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).username()).isEqualTo("user1");
        assertThat(responses.get(1).email()).isEqualTo("user2@example.com");
    }
}
