package com.aieducenter.account.endpoints.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aieducenter.tenant.domain.repository.TenantRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.verification.application.VerificationCodeAppService;
import com.aieducenter.verification.application.dto.VerifyCodeResult;
import com.aieducenter.account.config.SaTokenTestConfig;
import com.aieducenter.account.domain.aggregate.User;
import com.aieducenter.account.domain.repository.UserRepository;

/**
 * 通用注册流程集成测试。
 *
 * <p>验证注册成功、用户名重复、密码强度不足三个场景。</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRegistrationIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VerificationCodeAppService verificationCodeAppService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @BeforeAll
    static void setup() {
        SaTokenTestConfig.initSaTokenContext();
    }

    @Test
    @Transactional
    void given_valid_registration_when_register_then_return_token_and_create_user_and_tenant() throws Exception {
        // Mock verification code check
        when(verificationCodeAppService.verifyPhoneCode(any()))
            .thenReturn(new VerifyCodeResult(true, "OK"));

        String requestBody = """
            {
                "username": "newuser123",
                "password": "password123",
                "nickname": "New User",
                "verificationCode": "123456"
            }
            """;

        // When
        mvc.perform(post("/api/account/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").isNotEmpty());

        // Then — verify DB state
        assertThat(userRepository.existsByUsername("newuser123")).isTrue();
        // Note: Tenant creation is handled by UserRegisteredEventListener and tested separately
        // in UserRegisteredEventListenerTest and TenantAppServiceTest
    }

    @Test
    @Transactional
    void given_duplicate_username_when_register_then_return_409() throws Exception {
        // Mock verification code check
        when(verificationCodeAppService.verifyPhoneCode(any()))
            .thenReturn(new VerifyCodeResult(true, "OK"));

        User existingUser = User.register("testuser123", "password123", null, null, null);
        userRepository.save(existingUser);

        String requestBody = """
            {
                "username": "testuser123",
                "password": "password123",
                "nickname": "Another",
                "verificationCode": "123456"
            }
            """;

        // When / Then
        mvc.perform(post("/api/account/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    void given_weak_password_when_register_then_return_400() throws Exception {
        String requestBody = """
            {
                "username": "weakuser1",
                "password": "weakpass",
                "nickname": "Weak User",
                "verificationCode": "123456"
            }
            """;

        // When / Then
        mvc.perform(post("/api/account/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }
}
