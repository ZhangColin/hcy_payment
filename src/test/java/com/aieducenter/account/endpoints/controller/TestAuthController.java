package com.aieducenter.account.endpoints.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.stp.StpUtil;
import com.cartisan.security.annotation.CurrentUser;
import com.cartisan.security.annotation.RequireAuth;
import com.cartisan.web.response.ApiResponse;

/**
 * 测试专用 Controller，验证 cartisan-security 注解和 @CurrentUser 注入。
 *
 * <p>仅存在于 src/test/java，不打包进生产环境。</p>
 */
@RestController
@RequestMapping("/test/auth")
public class TestAuthController {

    @GetMapping("/login/{userId}")
    public ApiResponse<Map<String, String>> login(@PathVariable Long userId) {
        StpUtil.login(userId);
        Map<String, String> result = new HashMap<>();
        result.put("token", StpUtil.getTokenValue());
        return ApiResponse.ok(result);
    }

    @GetMapping("/require-auth")
    @RequireAuth
    public ApiResponse<String> requireAuth() {
        return ApiResponse.ok("authenticated");
    }

    @GetMapping("/public")
    public ApiResponse<String> publicEndpoint() {
        return ApiResponse.ok("public");
    }

    @GetMapping("/current-user-id")
    @RequireAuth
    public ApiResponse<Map<String, Object>> getCurrentUserId(@CurrentUser Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        return ApiResponse.ok(result);
    }

    @GetMapping("/current-user-id-optional")
    public ApiResponse<Map<String, Object>> getCurrentUserIdOptional(@CurrentUser Optional<Long> userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId.orElse(null));
        result.put("isPresent", userId.isPresent());
        return ApiResponse.ok(result);
    }

    @PostMapping("/logout")
    @RequireAuth
    public ApiResponse<Void> logout() {
        StpUtil.logout();
        return ApiResponse.ok(null);
    }
}
