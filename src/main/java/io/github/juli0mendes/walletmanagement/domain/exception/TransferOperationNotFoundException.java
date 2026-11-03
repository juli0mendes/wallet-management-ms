package io.github.juli0mendes.walletmanagement.domain.exception;

public class TransferOperationNotFoundException extends RuntimeException {
    public TransferOperationNotFoundException(String operationId) {
        super("Transfer operation not found for operation id: " + operationId);
    }
}
