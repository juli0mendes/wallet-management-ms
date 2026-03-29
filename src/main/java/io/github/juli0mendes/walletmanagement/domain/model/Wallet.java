package io.github.juli0mendes.walletmanagement.domain.model;

import io.github.juli0mendes.walletmanagement.domain.exception.InsufficientBalanceException;
import io.github.juli0mendes.walletmanagement.domain.exception.InvalidWalletException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Wallet {
    private static final int MIN_OWNER_NAME_LENGTH = 3;

    private final UUID id;
    private final String ownerName;
    private final String ownerEmail;
    private BigDecimal balance;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Wallet(UUID id, String ownerName, String ownerEmail, BigDecimal balance,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = validateId(id);
        this.ownerName = validateOwnerName(ownerName);
        this.ownerEmail = validateOwnerEmail(ownerEmail);
        this.balance = validateBalance(balance);
        this.createdAt = validateCreatedAt(createdAt);
        this.updatedAt = validateUpdatedAt(updatedAt);
    }

    public UUID getId() { return id; }

    public String getOwnerName() { return ownerName; }

    public String getOwnerEmail() { return ownerEmail; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void deposit(BigDecimal amount) {
        BigDecimal validated = validateAmount(amount, "Deposit amount must be greater than zero");
        this.balance = this.balance.add(validated);
        this.updatedAt = LocalDateTime.now();
    }

    public void withdraw(BigDecimal amount) {
        BigDecimal validated = validateAmount(amount, "Withdraw amount must be greater than zero");
        if (this.balance.compareTo(validated) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for this operation");
        }
        this.balance = this.balance.subtract(validated);
        this.updatedAt = LocalDateTime.now();
    }

    private static UUID validateId(UUID id) {
        if (id == null) throw new InvalidWalletException("Wallet id is required");
        return id;
    }

    private static String validateOwnerName(String ownerName) {
        if (ownerName == null || ownerName.isBlank()) {
            throw new InvalidWalletException("Owner name is required");
        }
        String normalized = ownerName.trim();
        if (normalized.length() < MIN_OWNER_NAME_LENGTH) {
            throw new InvalidWalletException("Owner name must have at least 3 characters");
        }
        return normalized;
    }

    private static String validateOwnerEmail(String ownerEmail) {
        if (ownerEmail == null || ownerEmail.isBlank()) {
            throw new InvalidWalletException("Owner email is required");
        }
        return ownerEmail;
    }

    private static BigDecimal validateBalance(BigDecimal balance) {
        if (balance == null) throw new InvalidWalletException("Balance is required");
        if (balance.compareTo(BigDecimal.ZERO) < 0) throw new InvalidWalletException("Balance cannot be negative");
        return balance;
    }

    private static LocalDateTime validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) throw new InvalidWalletException("Created at is required");
        return createdAt;
    }

    private static LocalDateTime validateUpdatedAt(LocalDateTime updatedAt) {
        if (updatedAt == null) throw new InvalidWalletException("Updated at is required");
        return updatedAt;
    }

    private static BigDecimal validateAmount(BigDecimal amount, String message) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidWalletException(message);
        }
        return amount;
    }
}
