package com.aieducenter.security.domain.error;

import com.cartisan.core.exception.CodeMessage;

public enum SecurityMessage implements CodeMessage {
    // ========== 格式校验错误 (400) ==========
    API_KEY_INVALID(400, "SEC_001", "API Key 格式不正确"),
    SIGNATURE_INVALID(400, "SEC_002", "签名验证失败"),
    TIMESTAMP_INVALID(400, "SEC_003", "时间戳格式不正确"),

    // ========== 唯一性错误 (409) ==========
    API_KEY_ALREADY_EXISTS(409, "SEC_010", "API Key 已存在"),

    // ========== 资源不存在 (404) ==========
    API_KEY_NOT_FOUND(404, "SEC_020", "API Key 不存在"),

    // ========== 业务限制 (403) ==========
    API_KEY_DISABLED(403, "SEC_030", "API Key 已禁用"),
    PERMISSION_DENIED(403, "SEC_031", "权限不足");

    private final int httpStatus;
    private final String code;
    private final String message;

    SecurityMessage(int httpStatus, String code, String message) {
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
