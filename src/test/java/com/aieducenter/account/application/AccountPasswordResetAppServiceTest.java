package com.aieducenter.account.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aieducenter.account.application.dto.command.ResetPasswordCommand;
import com.aieducenter.account.domain.aggregate.User;
import com.aieducenter.account.infrastructure.verification.VerificationCodePort;
import com.aieducenter.account.domain.repository.UserRepository;
import com.aieducenter.account.domain.service.AccountPasswordEncoderService;
import com.aieducenter.verification.domain.error.VerificationCodeError;
import com.cartisan.core.exception.ApplicationException;
import com.cartisan.security.authentication.AuthenticationService;

@ExtendWith(MockitoExtension.class)
class AccountPasswordResetAppServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationCodePort verificationCodePort;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private AccountPasswordEncoderService accountPasswordEncoderService;

    private AccountPasswordResetAppService resetAppService;

    @BeforeEach
    void setUp() {
        resetAppService = new AccountPasswordResetAppService(
            userRepository,
            verificationCodePort,
            authenticationService,
            accountPasswordEncoderService
        );
    }

    // BCrypt hash of "oldPassword1" — used as a placeholder stored password
    private static final String HASH_OLD_PASSWORD =
        "$2a$10$blloEbxpkrK8297ectXfNubsI5EUdub2zl.vThFLy.G4Dqyj37PXe";

    @Test
    void given_email_account_when_reset_password_then_verifyCode_and_findByEmail_and_kickout_called() {
        // Given
        var user = User.restore(2001L, "testuser", "test@example.com", null, HASH_OLD_PASSWORD, "Test", null);
        var command = new ResetPasswordCommand("test@example.com", "654321", "newPassword1");
        var encodedNewPassword = "$2a$10$newEncodedPassword";

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(accountPasswordEncoderService.encodePassword("newPassword1")).thenReturn(encodedNewPassword);

        // When
        resetAppService.resetPassword(command);

        // Then
        verify(verificationCodePort).verifyCode("test@example.com", "654321", "RESET_PASSWORD");
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(user);
        verify(authenticationService).kickout(2001L);
        verify(accountPasswordEncoderService).encodePassword("newPassword1");
    }

    @Test
    void given_phone_account_when_reset_password_then_verifyPhoneCode_and_findByPhoneNumber_called() {
        // Given
        var user = User.restore(2002L, "testuser", null, "13812345678", HASH_OLD_PASSWORD, "Test", null);
        var command = new ResetPasswordCommand("13812345678", "654321", "newPassword1");
        var encodedNewPassword = "$2a$10$newEncodedPassword";

        when(userRepository.findByPhoneNumber("13812345678")).thenReturn(Optional.of(user));
        when(accountPasswordEncoderService.encodePassword("newPassword1")).thenReturn(encodedNewPassword);

        // When
        resetAppService.resetPassword(command);

        // Then
        verify(verificationCodePort).verifyPhoneCode("13812345678", "654321", "RESET_PASSWORD");
        verify(userRepository).findByPhoneNumber("13812345678");
        verify(userRepository).save(user);
        verify(authenticationService).kickout(2002L);
        verify(accountPasswordEncoderService).encodePassword("newPassword1");
    }

    @Test
    void given_invalid_verification_code_when_reset_password_then_throw_verification_error() {
        // Given
        var command = new ResetPasswordCommand("test@example.com", "000000", "newPassword1");

        doThrow(new ApplicationException(VerificationCodeError.CODE_INVALID))
            .when(verificationCodePort).verifyCode("test@example.com", "000000", "RESET_PASSWORD");

        // When & Then
        assertThatThrownBy(() -> resetAppService.resetPassword(command))
            .isInstanceOf(ApplicationException.class)
            .hasMessageContaining(VerificationCodeError.CODE_INVALID.message());
    }

    @Test
    void given_nonexistent_account_when_reset_password_then_throw_code_invalid() {
        // Given
        var command = new ResetPasswordCommand("ghost@example.com", "654321", "newPassword1");

        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resetAppService.resetPassword(command))
            .isInstanceOf(ApplicationException.class)
            .hasMessageContaining(VerificationCodeError.CODE_INVALID.message());
    }

    @Test
    void given_kickout_fails_when_reset_password_then_no_exception_thrown() {
        // Given
        var user = User.restore(2003L, "testuser", "resilient@example.com", null, HASH_OLD_PASSWORD, "Test", null);
        var command = new ResetPasswordCommand("resilient@example.com", "654321", "newPassword1");
        var encodedNewPassword = "$2a$10$newEncodedPassword";

        when(userRepository.findByEmail("resilient@example.com")).thenReturn(Optional.of(user));
        when(accountPasswordEncoderService.encodePassword("newPassword1")).thenReturn(encodedNewPassword);
        doThrow(new RuntimeException("kickout error")).when(authenticationService).kickout(any());

        // When & Then — should complete without throwing
        assertThatCode(() -> resetAppService.resetPassword(command)).doesNotThrowAnyException();
    }
}
