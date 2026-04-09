package com.aieducenter.openapi.web;

import com.aieducenter.openapi.application.ApiKeyQueryAppService;
import com.aieducenter.openapi.domain.aggregate.ApiKey;
import com.aieducenter.openapi.domain.error.OpenApiMessage;
import com.aieducenter.openapi.domain.service.SignatureService;
import com.cartisan.core.exception.ApplicationException;
import com.cartisan.web.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 签名验证拦截器
 *
 * <p>检查方法/类上的 @RequireSignature 注解，有则执行签名验证</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SignatureVerificationInterceptor implements HandlerInterceptor {

    private final SignatureService signatureService;
    private final ApiKeyQueryAppService apiKeyQueryAppService;
    private final OpenApiSignatureConfig config;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理 Controller 方法
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 检查注解（方法级或类级）
        RequireSignature methodAnnotation = handlerMethod.getMethodAnnotation(RequireSignature.class);
        RequireSignature classAnnotation = handlerMethod.getBeanType().getAnnotation(RequireSignature.class);
        if (methodAnnotation == null && classAnnotation == null) {
            return true;
        }

        // 检查开关
        if (!config.isEnabled()) {
            return true;
        }

        try {
            // 1. 提取并校验必要参数
            String appId = getRequiredHeader(request, "X-App-Id");
            String timestamp = getRequiredHeader(request, "X-Timestamp");
            String nonce = getRequiredHeader(request, "X-Nonce");
            String sign = getRequiredHeader(request, "X-Sign");
            String clientBodyDigest = getRequiredHeader(request, "X-Body-Digest");

            // 2. 身份校验
            ApiKey apiKey = apiKeyQueryAppService.findByApiKeyForVerification(appId);

            // 3. 计算 server body digest
            byte[] body = getCachedBody(request);
            String serverBodyDigest = signatureService.calculateBodyDigest(body);

            // 4. 提取 query 参数
            Map<String, String> queryParams = extractQueryParams(request);

            // 5. 签名验证
            signatureService.verify(
                apiKey, timestamp, nonce, sign,
                clientBodyDigest, serverBodyDigest,
                queryParams, config.getTimestampTolerance()
            );

            // 6. 验证通过，放入 request attribute
            request.setAttribute("verifiedAppId", apiKey.getApiKey());
            request.setAttribute("verifiedBusinessSystemName", apiKey.getBusinessSystemName());

            return true;

        } catch (ApplicationException e) {
            writeErrorResponse(response, e.getCodeMessage().httpStatus(), e.getCodeMessage());
            return false;
        }
    }

    private String getRequiredHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (value == null || value.isBlank()) {
            throw new ApplicationException(OpenApiMessage.SIGNATURE_PARAMS_MISSING);
        }
        return value;
    }

    private byte[] getCachedBody(HttpServletRequest request) {
        if (request instanceof CachingRequestBodyFilter.CachedBodyHttpServletRequest cachedRequest) {
            return cachedRequest.getCachedBody();
        }
        return new byte[0];
    }

    private Map<String, String> extractQueryParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isBlank()) {
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                    params.put(key, value);
                }
            }
        }
        return params;
    }

    private void writeErrorResponse(HttpServletResponse response, int httpStatus, com.cartisan.core.exception.CodeMessage codeMessage) throws Exception {
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
            ApiResponse.error(codeMessage)
        ));
    }
}
