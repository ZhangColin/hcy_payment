package com.aieducenter.openapi.endpoints.api.v1;

import com.aieducenter.openapi.application.ApiKeyManagementAppService;
import com.aieducenter.openapi.application.dto.command.CreateApiKeyCommand;
import com.aieducenter.openapi.application.dto.response.ApiKeyResponse;
import com.cartisan.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * OpenAPI API v1 控制器
 *
 * <p>API Key 管理接口</p>
 */
@RestController
@RequestMapping("/api/v1/openapi/api-keys")
@RequiredArgsConstructor
@Validated
@Tag(name = "OpenAPI API v1", description = "API Key 管理接口")
public class OpenApiApiV1Controller {

    private final ApiKeyManagementAppService apiKeyManagementAppService;

    @PostMapping
    @Operation(summary = "创建 API Key")
    public ApiResponse<ApiKeyResponse> createApiKey(
            @Valid @RequestBody CreateApiKeyCommand command
    ) {
        ApiKeyResponse response = apiKeyManagementAppService.createApiKey(command);
        return ApiResponse.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询 API Key")
    public ApiResponse<ApiKeyResponse> getApiKey(
            @PathVariable Long id
    ) {
        ApiKeyResponse response = apiKeyManagementAppService.getApiKey(id);
        return ApiResponse.ok(response);
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用 API Key")
    public ApiResponse<Void> disableApiKey(
            @PathVariable Long id
    ) {
        apiKeyManagementAppService.disableApiKey(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/enable")
    @Operation(summary = "启用 API Key")
    public ApiResponse<Void> enableApiKey(
            @PathVariable Long id
    ) {
        apiKeyManagementAppService.enableApiKey(id);
        return ApiResponse.ok();
    }
}
