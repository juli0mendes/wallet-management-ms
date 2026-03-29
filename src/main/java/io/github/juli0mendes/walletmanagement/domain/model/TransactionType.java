package io.github.juli0mendes.walletmanagement.domain.model;

public enum TransactionType {
    DEPOSIT("DEPOSIT"),
    WITHDRAW("WITHDRAW"),
    TRANSFER_IN("TRANSFER_IN"),
    TRANSFER_OUT("TRANSFER_OUT");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
