package io.github.juli0mendes.walletmanagement.domain.model;

public enum TransferOperationStatus {
    INITIATED,
    APPLIED,
    REJECTED,
    UNKNOWN_COMMIT_RESULT,
    RECONCILING;

    public boolean isTerminal() {
        return this == APPLIED || this == REJECTED;
    }

    public static boolean canTransition(TransferOperationStatus current, TransferOperationStatus next) {
        if (current == null || next == null) {
            return false;
        }

        if (current.isTerminal()) {
            return false;
        }

        return switch (current) {
            case INITIATED -> next == APPLIED || next == REJECTED || next == UNKNOWN_COMMIT_RESULT;
            case UNKNOWN_COMMIT_RESULT -> next == RECONCILING;
            case RECONCILING -> next == APPLIED || next == UNKNOWN_COMMIT_RESULT || next == RECONCILING;
            case APPLIED, REJECTED -> false;
        };
    }
}
