package com.aieducenter.tenant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.aieducenter.account.application.dto.event.UserRegisteredEvent;
import com.aieducenter.tenant.endpoints.listener.UserRegisteredEventListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aieducenter.tenant.domain.aggregate.Tenant;
import com.aieducenter.tenant.domain.enums.TenantType;
import com.aieducenter.tenant.domain.repository.TenantRepository;

@ExtendWith(MockitoExtension.class)
class TenantAppServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantAppService tenantAppService;

    @Test
    void given_validOwnerAndName_when_createPersonalTenant_then_savesCorrectTenant() {
        // When
        tenantAppService.createPersonalTenant(123L, "John's Space");

        // Then
        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(captor.capture());
        Tenant saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("John's Space");
        assertThat(saved.getType()).isEqualTo(TenantType.PERSONAL);
        assertThat(saved.getOwnerId()).isEqualTo(123L);
    }

    @Test
    void given_nicknameNotBlank_when_onUserRegistered_then_useNicknameAsTenantName() {
        // Given
        TenantRepository mockRepo = org.mockito.Mockito.mock(TenantRepository.class);
        TenantAppService service = new TenantAppService(mockRepo);
        UserRegisteredEventListener listener = new UserRegisteredEventListener(service);
        UserRegisteredEvent event = new UserRegisteredEvent(
                1L, "john_doe", "john@example.com", "John Doe");

        // When
        listener.onUserRegistered(event);

        // Then
        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(mockRepo).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("John Doe");
        assertThat(captor.getValue().getOwnerId()).isEqualTo(1L);
    }

    @Test
    void given_nicknameBlank_when_onUserRegistered_then_useUsernameAsTenantName() {
        // Given
        TenantRepository mockRepo = org.mockito.Mockito.mock(TenantRepository.class);
        TenantAppService service = new TenantAppService(mockRepo);
        UserRegisteredEventListener listener = new UserRegisteredEventListener(service);
        UserRegisteredEvent event = new UserRegisteredEvent(
                2L, "jane_doe", "jane@example.com", null);

        // When
        listener.onUserRegistered(event);

        // Then
        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(mockRepo).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("jane_doe");
        assertThat(captor.getValue().getOwnerId()).isEqualTo(2L);
    }
}
