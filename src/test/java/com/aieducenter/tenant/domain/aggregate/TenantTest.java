package com.aieducenter.tenant.domain.aggregate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.aieducenter.tenant.domain.enums.TenantType;

class TenantTest {

    @Test
    void given_valid_fields_when_create_tenant_then_success() {
        // Given
        String name = "John's Space";
        TenantType type = TenantType.PERSONAL;
        Long ownerId = 123L;

        // When
        Tenant tenant = new Tenant(name, type, ownerId);

        // Then
        assertThat(tenant.getName()).isEqualTo(name);
        assertThat(tenant.getType()).isEqualTo(type);
        assertThat(tenant.getOwnerId()).isEqualTo(ownerId);
    }
}
