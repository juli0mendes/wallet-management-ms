package io.github.juli0mendes.walletmanagement.exception;

import io.github.juli0mendes.walletmanagement.domain.exception.InsufficientBalanceException;
import io.github.juli0mendes.walletmanagement.domain.exception.InvalidWalletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidWalletException.class)
    public ResponseEntity<ErrorResponse> handleInvalidWallet(
            InvalidWalletException ex, WebRequest request) {
        log.warn("Invalid wallet operation path={} message={}",
                request.getDescription(false).replace("uri=", ""), ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_WALLET",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(
            InsufficientBalanceException ex, WebRequest request) {
        log.warn("Insufficient balance path={} message={}",
                request.getDescription(false).replace("uri=", ""), ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "INSUFFICIENT_BALANCE",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFound(
            WalletNotFoundException ex, WebRequest request) {
        log.warn("Wallet not found path={} message={}",
                request.getDescription(false).replace("uri=", ""), ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "WALLET_NOT_FOUND",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation failed path={} errorCount={}",
                request.getDescription(false).replace("uri=", ""), ex.getBindingResult().getErrorCount());
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error_code", "VALIDATION_ERROR");
        errorResponse.put("message", "Validation failed");
        errorResponse.put("timestamp", System.currentTimeMillis());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        errorResponse.put("errors", fieldErrors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unhandled exception path={} type={} message={}",
                request.getDescription(false).replace("uri=", ""), ex.getClass().getSimpleName(), ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

