package com.aieducenter.openapi.web;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 签名验证配置属性
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "openapi.signature")
public class OpenApiSignatureConfig {

    /**
     * 时间戳容忍窗口（秒）
     */
    private long timestampTolerance = 300;

    /**
     * Nonce 过期时间（秒）
     */
    private long nonceTtl = 300;

    /**
     * 是否启用签名验证
     */
    private boolean enabled = true;
}
