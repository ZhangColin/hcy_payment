package com.aieducenter.openapi.domain.error;

import com.cartisan.core.exception.CodeMessage;

public enum OpenApiMessage implements CodeMessage {
    // ========== 格式校验错误 (400) ==========
    API_KEY_INVALID(400, "OAS_001", "API Key 格式不正确"),
    SIGNATURE_INVALID(400, "OAS_002", "签名验证失败"),
    TIMESTAMP_INVALID(400, "OAS_003", "时间戳格式不正确"),

    // ========== 唯一性错误 (409) ==========
    API_KEY_ALREADY_EXISTS(409, "OAS_010", "API Key 已存在"),

    // ========== 资源不存在 (404) ==========
    API_KEY_NOT_FOUND(404, "OAS_020", "API Key 不存在"),

    // ========== 签名验证错误 (401) ==========
    SIGNATURE_PARAMS_MISSING(401, "OAS_040", "缺少签名验证必要参数"),
    SIGNATURE_TIMESTAMP_EXPIRED(401, "OAS_041", "请求时间戳已过期"),
    SIGNATURE_NONCE_DUPLICATED(401, "OAS_042", "重复的请求"),
    SIGNATURE_API_KEY_INVALID(401, "OAS_043", "API Key 无效或已禁用"),
    SIGNATURE_BODY_DIGEST_MISMATCH(401, "OAS_044", "请求体摘要不匹配"),
    SIGNATURE_MISMATCH(401, "OAS_045", "签名不匹配"),

    // ========== 业务限制 (403) ==========
    API_KEY_DISABLED(403, "OAS_030", "API Key 已禁用"),
    PERMISSION_DENIED(403, "OAS_031", "权限不足");

    private final int httpStatus;
    private final String code;
    private final String message;

    OpenApiMessage(int httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public int httpStatus() {
        return httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
