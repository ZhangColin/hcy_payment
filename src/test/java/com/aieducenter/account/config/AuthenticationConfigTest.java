package com.aieducenter.account.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 认证基础设施配置测试。
 *
 * <p>验证 cartisan-security 自动配置正确加载，关键 Bean 存在于上下文中。</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AuthenticationConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void given_cartisan_security_configured_when_context_loads_then_security_interceptor_present() {
        assertThat(context.containsBean("securityInterceptor")).isTrue();
    }

    @Test
    void given_cartisan_security_configured_when_context_loads_then_exception_handler_present() {
        assertThat(context.containsBean("securityExceptionHandler")).isTrue();
    }

    @Test
    void given_cartisan_security_configured_when_context_loads_then_sa_token_context_filter_present() {
        assertThat(context.containsBean("saTokenContextFilter")).isTrue();
    }
}
