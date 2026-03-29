package io.github.juli0mendes.walletmanagement.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ErrorResponse Unit Tests")
class ErrorResponseTest {

    @Test
    @DisplayName("Should create ErrorResponse with constructor")
    void shouldCreateErrorResponseWithConstructor() {
        String errorCode = "TEST_ERROR";
        String message = "Test error message";
        String path = "/api/test";

        ErrorResponse errorResponse = new ErrorResponse(errorCode, message, path);

        assertEquals(errorCode, errorResponse.getErrorCode());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("Should create ErrorResponse with default constructor")
    void shouldCreateErrorResponseWithDefaultConstructor() {
        ErrorResponse errorResponse = new ErrorResponse();

        assertNull(errorResponse.getErrorCode());
        assertNull(errorResponse.getMessage());
        assertNull(errorResponse.getPath());
        assertNull(errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("Should set and get error code")
    void shouldSetAndGetErrorCode() {
        ErrorResponse errorResponse = new ErrorResponse();
        String errorCode = "VALIDATION_ERROR";

        errorResponse.setErrorCode(errorCode);

        assertEquals(errorCode, errorResponse.getErrorCode());
    }

    @Test
    @DisplayName("Should set and get message")
    void shouldSetAndGetMessage() {
        ErrorResponse errorResponse = new ErrorResponse();
        String message = "Error message";

        errorResponse.setMessage(message);

        assertEquals(message, errorResponse.getMessage());
    }

    @Test
    @DisplayName("Should set and get timestamp")
    void shouldSetAndGetTimestamp() {
        ErrorResponse errorResponse = new ErrorResponse();
        LocalDateTime timestamp = LocalDateTime.now();

        errorResponse.setTimestamp(timestamp);

        assertEquals(timestamp, errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("Should set and get path")
    void shouldSetAndGetPath() {
        ErrorResponse errorResponse = new ErrorResponse();
        String path = "/api/test";

        errorResponse.setPath(path);

        assertEquals(path, errorResponse.getPath());
    }
}
