package io.github.juli0mendes.walletmanagement.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Should handle WalletNotFoundException correctly")
    void shouldHandleWalletNotFoundExceptionCorrectly() {
        var walletId = UUID.randomUUID();
        var exception = new WalletNotFoundException(walletId.toString());
        var request = createMockWebRequest("/api/v1/test/wallet-not-found/" + walletId);

        var response = exceptionHandler.handleWalletNotFound(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("WALLET_NOT_FOUND", response.getBody().getErrorCode());
        assertEquals("Wallet not found with id: " + walletId, response.getBody().getMessage());
        assertEquals("/api/v1/test/wallet-not-found/" + walletId, response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Should handle validation exceptions correctly")
    void shouldHandleValidationExceptionsCorrectly() {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "testField", "Test validation error"));
        var exception = new MethodArgumentNotValidException(null, bindingResult);
        var request = createMockWebRequest("/api/v1/test/validation-error");

        var response = exceptionHandler.handleValidationException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().get("error_code"));
        assertEquals("Validation failed", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
        assertNotNull(response.getBody().get("errors"));
        @SuppressWarnings("unchecked")
        var errors = (java.util.Map<String, String>) response.getBody().get("errors");
        assertEquals("Test validation error", errors.get("testField"));
    }

    @Test
    @DisplayName("Should handle validation exceptions with multiple errors")
    void shouldHandleValidationExceptionsWithMultipleErrors() {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "field1", "Error 1"));
        bindingResult.addError(new FieldError("testObject", "field2", "Error 2"));
        var exception = new MethodArgumentNotValidException(null, bindingResult);
        var request = createMockWebRequest("/api/v1/test/validation-error");

        var response = exceptionHandler.handleValidationException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        var errors = (java.util.Map<String, String>) response.getBody().get("errors");
        assertEquals(2, errors.size());
        assertEquals("Error 1", errors.get("field1"));
        assertEquals("Error 2", errors.get("field2"));
    }

    @Test
    @DisplayName("Should handle generic exceptions correctly")
    void shouldHandleGenericExceptionsCorrectly() {
        var exception = new RuntimeException("Test generic error");
        var request = createMockWebRequest("/api/v1/test/generic-error");

        var response = exceptionHandler.handleGlobalException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertEquals("/api/v1/test/generic-error", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Should handle null pointer exception as generic exception")
    void shouldHandleNullPointerExceptionAsGenericException() {
        var exception = new NullPointerException("Null pointer test");
        var request = createMockWebRequest("/api/v1/test/null-error");

        var response = exceptionHandler.handleGlobalException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle WalletNotFoundException with custom message")
    void shouldHandleWalletNotFoundExceptionWithCustomMessage() {
        var customMessage = "Custom wallet not found message";
        var exception = new WalletNotFoundException(customMessage, null);
        var request = createMockWebRequest("/api/v1/test/custom-error");

        var response = exceptionHandler.handleWalletNotFound(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("WALLET_NOT_FOUND", response.getBody().getErrorCode());
        assertEquals(customMessage, response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle validation exception with empty binding result")
    void shouldHandleValidationExceptionWithEmptyBindingResult() {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "emptyObject");
        var exception = new MethodArgumentNotValidException(null, bindingResult);
        var request = createMockWebRequest("/api/v1/test/empty-validation");

        var response = exceptionHandler.handleValidationException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        var errors = (java.util.Map<String, String>) response.getBody().get("errors");
        assertTrue(errors.isEmpty());
    }

    private WebRequest createMockWebRequest(String uri) {
        var mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI(uri);
        return new ServletWebRequest(mockRequest);
    }
}
