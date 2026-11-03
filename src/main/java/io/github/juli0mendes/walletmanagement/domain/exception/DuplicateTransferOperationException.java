package io.github.juli0mendes.walletmanagement.domain.exception;

public class DuplicateTransferOperationException extends RuntimeException {
    public DuplicateTransferOperationException(String message) {
        super(message);
    }
}
