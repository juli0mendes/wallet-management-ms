package io.github.juli0mendes.walletmanagement.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WalletNotFoundException Unit Tests")
class WalletNotFoundExceptionTest {

    @Test
    @DisplayName("Should create exception with wallet ID")
    void shouldCreateExceptionWithWalletId() {
        String walletId = "test-wallet-id";

        WalletNotFoundException exception = new WalletNotFoundException(walletId);

        assertEquals("Wallet not found with id: " + walletId, exception.getMessage());
    }

    @Test
    @DisplayName("Should create exception with custom message and cause")
    void shouldCreateExceptionWithCustomMessageAndCause() {
        String customMessage = "Custom error message";
        Throwable cause = new RuntimeException("Root cause");

        WalletNotFoundException exception = new WalletNotFoundException(customMessage, cause);

        assertEquals(customMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should handle null wallet ID")
    void shouldHandleNullWalletId() {
        String walletId = null;

        WalletNotFoundException exception = new WalletNotFoundException(walletId);

        assertEquals("Wallet not found with id: null", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle empty wallet ID")
    void shouldHandleEmptyWalletId() {
        String walletId = "";

        WalletNotFoundException exception = new WalletNotFoundException(walletId);

        assertEquals("Wallet not found with id: ", exception.getMessage());
    }
}
