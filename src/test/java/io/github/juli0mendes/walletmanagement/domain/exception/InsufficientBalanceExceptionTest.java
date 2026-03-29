package io.github.juli0mendes.walletmanagement.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InsufficientBalanceException Unit Tests")
class InsufficientBalanceExceptionTest {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
        String message = "Insufficient balance for this operation";

        InsufficientBalanceException exception = new InsufficientBalanceException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Should be a RuntimeException subtype")
    void shouldBeARuntimeException() {
        InsufficientBalanceException exception = new InsufficientBalanceException("err");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("Should preserve exact message")
    void shouldPreserveExactMessage() {
        String msg = "Balance is 0.00, required 50.00";
        InsufficientBalanceException exception = new InsufficientBalanceException(msg);
        assertEquals(msg, exception.getMessage());
    }

    @Test
    @DisplayName("Should have null cause by default")
    void shouldHaveNullCauseByDefault() {
        InsufficientBalanceException exception = new InsufficientBalanceException("err");
        assertNull(exception.getCause());
    }
}

