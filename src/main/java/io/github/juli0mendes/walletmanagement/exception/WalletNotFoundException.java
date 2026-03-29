package io.github.juli0mendes.walletmanagement.exception;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(String walletId) {
        super("Wallet not found with id: " + walletId);
    }

    public WalletNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

