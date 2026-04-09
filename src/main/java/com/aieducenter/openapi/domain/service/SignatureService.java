package com.aieducenter.openapi.domain.service;

import com.aieducenter.openapi.domain.aggregate.ApiKey;
import com.aieducenter.openapi.domain.error.OpenApiMessage;
import com.aieducenter.openapi.domain.port.NonceRepository;
import com.aieducenter.openapi.domain.port.SignatureCalculator;
import com.cartisan.core.exception.ApplicationException;
import com.cartisan.core.stereotype.DomainService;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.TreeMap;

/**
 * 签名验证领域服务
 */
@DomainService
@RequiredArgsConstructor
public class SignatureService {

    private final SignatureCalculator signatureCalculator;
    private final NonceRepository nonceRepository;

    /**
     * 校验时间戳（廉价检查，应在身份校验之前执行）
     */
    public void validateTimestamp(String timestamp, long timestampTolerance) {
        long requestTime = Long.parseLong(timestamp);
        long currentTime = System.currentTimeMillis() / 1000;
        if (Math.abs(currentTime - requestTime) > timestampTolerance) {
            throw new ApplicationException(OpenApiMessage.SIGNATURE_TIMESTAMP_EXPIRED);
        }
    }

    /**
     * Nonce 防重放校验（廉价检查，应在身份校验之前执行）
     */
    public void validateNonce(String nonce) {
        if (nonceRepository.existsAndStore(nonce)) {
            throw new ApplicationException(OpenApiMessage.SIGNATURE_NONCE_DUPLICATED);
        }
    }

    /**
     * 验证请求签名（Body Digest + 签名校验）
     *
     * <p>时间戳和 Nonce 校验由拦截器提前执行，此处只做 Body Digest 和签名校验</p>
     */
    public void verify(ApiKey apiKey, String timestamp, String nonce,
                       String clientSign, String clientBodyDigest, String serverBodyDigest,
                       Map<String, String> queryParams, long timestampTolerance) {
        // 1. Body Digest 校验
        if (!serverBodyDigest.equals(clientBodyDigest)) {
            throw new ApplicationException(OpenApiMessage.SIGNATURE_BODY_DIGEST_MISMATCH);
        }

        // 2. 签名校验
        String expectedSign = calculateSign(
            apiKey.getApiKey(), clientBodyDigest, nonce, timestamp, queryParams, apiKey.getApiSecret()
        );
        if (!expectedSign.equals(clientSign)) {
            throw new ApplicationException(OpenApiMessage.SIGNATURE_MISMATCH);
        }
    }

    /**
     * 计算签名
     */
    public String calculateSign(String appId, String bodyDigest, String nonce,
                                String timestamp, Map<String, String> queryParams,
                                String appSecret) {
        TreeMap<String, String> sortedParams = new TreeMap<>();
        sortedParams.put("appId", appId);
        sortedParams.put("bodyDigest", bodyDigest);
        sortedParams.put("nonce", nonce);
        sortedParams.put("timestamp", timestamp);
        if (queryParams != null) {
            sortedParams.putAll(queryParams);
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (!sb.isEmpty()) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }

        return signatureCalculator.calculate(sb.toString(), appSecret);
    }

    /**
     * 计算请求体的 SHA-256 摘要
     */
    public String calculateBodyDigest(byte[] body) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(body != null ? body : new byte[0]);
            return java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
