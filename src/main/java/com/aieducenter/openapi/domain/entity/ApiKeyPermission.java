package com.aieducenter.openapi.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "oas_api_key_permissions")
@Getter
public class ApiKeyPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permission", nullable = false, length = 128)
    private String permission;

    public ApiKeyPermission() {
    }

    public ApiKeyPermission(String permission) {
        this.permission = permission;
    }
}
