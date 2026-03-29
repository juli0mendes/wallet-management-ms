package io.github.juli0mendes.walletmanagement.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WalletNotFoundException Unit Tests")
class WalletNotFoundExceptionTest {

    @Test
    @DisplayName("Should create exception with wallet ID")
    void shouldCreateExceptionWithWalletId() {
        // Given
        String walletId = "test-wallet-id";

        // When
        WalletNotFoundException exception = new WalletNotFoundException(walletId);

        // Then
        assertEquals("Wallet not found with id: " + walletId, exception.getMessage());
    }

    @Test
    @DisplayName("Should create exception with custom message and cause")
    void shouldCreateExceptionWithCustomMessageAndCause() {
        // Given
        String customMessage = "Custom error message";
        Throwable cause = new RuntimeException("Root cause");

        // When
        WalletNotFoundException exception = new WalletNotFoundException(customMessage, cause);

        // Then
        assertEquals(customMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should handle null wallet ID")
    void shouldHandleNullWalletId() {
        // Given
        String walletId = null;

        // When
        WalletNotFoundException exception = new WalletNotFoundException(walletId);

        // Then
        assertEquals("Wallet not found with id: null", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle empty wallet ID")
    void shouldHandleEmptyWalletId() {
        // Given
        String walletId = "";

        // When
        WalletNotFoundException exception = new WalletNotFoundException(walletId);

        // Then
        assertEquals("Wallet not found with id: ", exception.getMessage());
    }
}
