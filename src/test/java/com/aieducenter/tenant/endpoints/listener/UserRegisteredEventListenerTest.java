package com.aieducenter.tenant.endpoints.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aieducenter.account.application.dto.event.UserRegisteredEvent;
import com.aieducenter.tenant.application.TenantAppService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserRegisteredEventListenerTest {

    @Mock
    private TenantAppService tenantAppService;

    @Test
    void given_userRegisteredEvent_withNickname_when_handle_then_createTenantWithNickname() {
        // Given
        UserRegisteredEventListener listener = new UserRegisteredEventListener(tenantAppService);
        Long userId = 100L;
        String username = "alice";
        String nickname = "爱丽丝";
        UserRegisteredEvent event = new UserRegisteredEvent(userId, username, "alice@example.com", nickname);

        // When
        listener.onUserRegistered(event);

        // Then
        verify(tenantAppService).createPersonalTenant(userId, nickname);
    }

    @Test
    void given_userRegisteredEvent_withoutNickname_when_handle_then_createTenantWithUsername() {
        // Given
        UserRegisteredEventListener listener = new UserRegisteredEventListener(tenantAppService);
        Long userId = 100L;
        String username = "alice";
        UserRegisteredEvent event = new UserRegisteredEvent(userId, username, null, null);

        // When
        listener.onUserRegistered(event);

        // Then
        verify(tenantAppService).createPersonalTenant(userId, username);
    }

    @Test
    void given_userRegisteredEvent_withBlankNickname_when_handle_then_createTenantWithUsername() {
        // Given
        UserRegisteredEventListener listener = new UserRegisteredEventListener(tenantAppService);
        Long userId = 100L;
        String username = "alice";
        String nickname = "   ";
        UserRegisteredEvent event = new UserRegisteredEvent(userId, username, "alice@example.com", nickname);

        // When
        listener.onUserRegistered(event);

        // Then
        verify(tenantAppService).createPersonalTenant(userId, username);
    }
}
