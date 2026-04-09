package com.aieducenter.openapi.domain.aggregate;

import cn.hutool.core.util.StrUtil;
import com.aieducenter.openapi.domain.entity.ApiKeyPermission;
import com.aieducenter.openapi.domain.enums.ApiKeyStatus;
import com.aieducenter.openapi.domain.error.OpenApiMessage;
import com.cartisan.core.domain.AggregateRoot;
import com.cartisan.core.stereotype.Aggregate;
import com.cartisan.core.util.Assertions;
import com.cartisan.data.jpa.domain.AuditableSoftDeletable;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.Set;

@Entity
@Table(name = "oas_api_keys")
@Aggregate
public class ApiKey extends AuditableSoftDeletable implements AggregateRoot<ApiKey, Long> {
    private static final String API_KEY_PATTERN = "^[A-Z0-9]{32}$";

    @Getter
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Getter
    @Column(name = "api_key", nullable = false, unique = true, length = 64)
    private String apiKey;

    @Getter
    @Column(name = "api_secret", nullable = false)
    private String apiSecret;

    @Getter
    @Column(name = "business_system_name", nullable = false, length = 128)
    private String businessSystemName;

    @Getter
    @Column(name = "status", nullable = false)
    private ApiKeyStatus status;

    @Getter
    @Column(name = "permissions", columnDefinition = "TEXT")
    private String permissions;

    @Getter
    @Column(name = "description")
    private String description;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "api_key_id")
    @Getter
    private final Set<ApiKeyPermission> apiKeyPermissions = cn.hutool.core.collection.CollUtil.newHashSet();

    @PrePersist
    void prePersist() {
        if (id == null) {
            this.id = com.cartisan.data.jpa.id.TsidGenerator.newInstance().generate();
        }
    }

    /**
     * 创建 API Key
     */
    public ApiKey(String businessSystemName, String description, Set<String> permissions) {
        Assertions.require(StrUtil.isNotBlank(businessSystemName),
            OpenApiMessage.API_KEY_INVALID);

        this.businessSystemName = businessSystemName;
        this.description = description;
        this.status = ApiKeyStatus.ACTIVE;
        this.apiKey = generateApiKey();
        this.apiSecret = generateApiSecret();

        if (permissions != null && !permissions.isEmpty()) {
            this.permissions = String.join(",", permissions);
            permissions.forEach(perm ->
                this.apiKeyPermissions.add(new ApiKeyPermission(perm))
            );
        }
    }

    /**
     * 禁用 API Key
     */
    public void disable() {
        this.status = ApiKeyStatus.DISABLED;
    }

    /**
     * 启用 API Key
     */
    public void enable() {
        this.status = ApiKeyStatus.ACTIVE;
    }

    /**
     * 验证权限
     */
    public boolean hasPermission(String permission) {
        if (status != ApiKeyStatus.ACTIVE) {
            return false;
        }
        if (permissions == null) {
            return false;
        }
        return permissions.contains(permission);
    }

    private String generateApiKey() {
        // 生成32位大写字母和数字组合
        return cn.hutool.core.util.RandomUtil.randomString("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", 32);
    }

    private String generateApiSecret() {
        // 生成32字节的密钥，然后Base64编码
        byte[] secretBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(secretBytes);
        return java.util.Base64.getEncoder().encodeToString(secretBytes);
    }
}
