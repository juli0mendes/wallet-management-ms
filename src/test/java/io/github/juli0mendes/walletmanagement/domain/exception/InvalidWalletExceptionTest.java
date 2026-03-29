package io.github.juli0mendes.walletmanagement.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InvalidWalletException Unit Tests")
class InvalidWalletExceptionTest {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
        String message = "Wallet id is required";

        InvalidWalletException exception = new InvalidWalletException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Should be a RuntimeException subtype")
    void shouldBeARuntimeException() {
        InvalidWalletException exception = new InvalidWalletException("err");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("Should preserve exact message for each validation scenario")
    void shouldPreserveExactMessageForEachValidationScenario() {
        String[] messages = {
                "Wallet id is required",
                "Owner name is required",
                "Owner email is required",
                "Balance is required",
                "Balance cannot be negative",
                "Created at is required",
                "Updated at is required",
                "Deposit amount must be greater than zero",
                "Withdraw amount must be greater than zero"
        };

        for (String msg : messages) {
            InvalidWalletException exception = new InvalidWalletException(msg);
            assertEquals(msg, exception.getMessage());
        }
    }

    @Test
    @DisplayName("Should have null cause by default")
    void shouldHaveNullCauseByDefault() {
        InvalidWalletException exception = new InvalidWalletException("err");
        assertNull(exception.getCause());
    }
}

