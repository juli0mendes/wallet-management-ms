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
        // Given
        String errorCode = "TEST_ERROR";
        String message = "Test error message";
        String path = "/api/test";

        // When
        ErrorResponse errorResponse = new ErrorResponse(errorCode, message, path);

        // Then
        assertEquals(errorCode, errorResponse.getErrorCode());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("Should create ErrorResponse with default constructor")
    void shouldCreateErrorResponseWithDefaultConstructor() {
        // When
        ErrorResponse errorResponse = new ErrorResponse();

        // Then
        assertNull(errorResponse.getErrorCode());
        assertNull(errorResponse.getMessage());
        assertNull(errorResponse.getPath());
        assertNull(errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("Should set and get error code")
    void shouldSetAndGetErrorCode() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        String errorCode = "VALIDATION_ERROR";

        // When
        errorResponse.setErrorCode(errorCode);

        // Then
        assertEquals(errorCode, errorResponse.getErrorCode());
    }

    @Test
    @DisplayName("Should set and get message")
    void shouldSetAndGetMessage() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        String message = "Error message";

        // When
        errorResponse.setMessage(message);

        // Then
        assertEquals(message, errorResponse.getMessage());
    }

    @Test
    @DisplayName("Should set and get timestamp")
    void shouldSetAndGetTimestamp() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        LocalDateTime timestamp = LocalDateTime.now();

        // When
        errorResponse.setTimestamp(timestamp);

        // Then
        assertEquals(timestamp, errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("Should set and get path")
    void shouldSetAndGetPath() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        String path = "/api/test";

        // When
        errorResponse.setPath(path);

        // Then
        assertEquals(path, errorResponse.getPath());
    }
}
