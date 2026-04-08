package com.aieducenter.account.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.dev33.satoken.stp.StpUtil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证基础设施集成测试。
 *
 * <p>验证 @RequireAuth、@CurrentUser 注解行为及 401/200 响应。</p>
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "cartisan.security.interceptor.path-patterns[0]=/api/**",
        "cartisan.security.interceptor.path-patterns[1]=/admin/**",
        "cartisan.security.interceptor.path-patterns[2]=/test/**",
        "sa-token.token-name=Authorization"
    }
)
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void initSaTokenContext() {
        SaTokenTestConfig.initSaTokenContext();
    }

    @Test
    void given_no_token_when_access_require_auth_endpoint_then_401() throws Exception {
        mvc.perform(get("/test/auth/require-auth"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void given_valid_token_when_access_require_auth_endpoint_then_200() throws Exception {
        String token = login(100L);
        try {
            mvc.perform(get("/test/auth/require-auth")
                    .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("authenticated"));
        } finally {
            StpUtil.logout(100L);
        }
    }

    @Test
    void given_valid_token_when_access_public_endpoint_then_200() throws Exception {
        mvc.perform(get("/test/auth/public"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value("public"));
    }

    @Test
    void given_valid_token_when_get_current_user_id_then_user_id_in_response() throws Exception {
        String token = login(42L);
        try {
            mvc.perform(get("/test/auth/current-user-id")
                    .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(42));
        } finally {
            StpUtil.logout(42L);
        }
    }

    @Test
    void given_no_token_when_get_current_user_id_optional_then_200_with_null() throws Exception {
        mvc.perform(get("/test/auth/current-user-id-optional"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isPresent").value(false));
    }

    @Test
    void given_valid_token_when_logout_then_token_invalid() throws Exception {
        String token = login(99L);
        mvc.perform(post("/test/auth/logout")
                .header("Authorization", token))
            .andExpect(status().isOk());

        mvc.perform(get("/test/auth/require-auth")
                .header("Authorization", token))
            .andExpect(status().isUnauthorized());
    }

    private String login(Long userId) throws Exception {
        String response = mvc.perform(get("/test/auth/login/" + userId))
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("token").asText();
    }
}
