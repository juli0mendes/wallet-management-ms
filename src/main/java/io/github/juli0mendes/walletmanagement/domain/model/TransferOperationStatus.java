package io.github.juli0mendes.walletmanagement.domain.model;

public enum TransferOperationStatus {
    INITIATED,
    APPLIED,
    REJECTED,
    UNKNOWN_COMMIT_RESULT,
    RECONCILING
}
