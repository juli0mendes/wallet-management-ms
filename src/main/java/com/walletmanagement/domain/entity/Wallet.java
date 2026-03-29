package com.walletmanagement.domain.entity;

import com.walletmanagement.domain.exception.InsufficientBalanceException;
import com.walletmanagement.domain.exception.InvalidWalletException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Wallet {

    private static final int MIN_OWNER_NAME_LENGTH = 3;

    private final UUID id;
    private final String ownerName;
    private BigDecimal balance;
    private final LocalDateTime createdAt;

    public Wallet(UUID id, String ownerName, BigDecimal balance, LocalDateTime createdAt) {
        this.id = validateId(id);
        this.ownerName = validateOwnerName(ownerName);
        this.balance = validateBalance(balance);
        this.createdAt = validateCreatedAt(createdAt);
    }

    public static Wallet create(String ownerName) {
        return new Wallet(UUID.randomUUID(), ownerName, BigDecimal.ZERO, LocalDateTime.now());
    }

    public UUID getId() {
        return id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void deposit(BigDecimal amount) {
        BigDecimal validatedAmount = validateAmount(amount, "Deposit amount must be greater than zero");
        this.balance = this.balance.add(validatedAmount);
    }

    public void withdraw(BigDecimal amount) {
        BigDecimal validatedAmount = validateAmount(amount, "Withdraw amount must be greater than zero");

        if (this.balance.compareTo(validatedAmount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for this operation");
        }

        this.balance = this.balance.subtract(validatedAmount);
    }

    private static UUID validateId(UUID id) {
        if (id == null) {
            throw new InvalidWalletException("Wallet id is required");
        }
        return id;
    }

    private static String validateOwnerName(String ownerName) {
        if (ownerName == null || ownerName.isBlank()) {
            throw new InvalidWalletException("Owner name is required");
        }

        String normalizedOwnerName = ownerName.trim();
        if (normalizedOwnerName.length() < MIN_OWNER_NAME_LENGTH) {
            throw new InvalidWalletException("Owner name must have at least 3 characters");
        }
        return normalizedOwnerName;
    }

    private static BigDecimal validateBalance(BigDecimal balance) {
        if (balance == null) {
            throw new InvalidWalletException("Balance is required");
        }
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidWalletException("Balance cannot be negative");
        }
        return balance;
    }

    private static LocalDateTime validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new InvalidWalletException("Created at is required");
        }
        return createdAt;
    }

    private static BigDecimal validateAmount(BigDecimal amount, String message) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidWalletException(message);
        }
        return amount;
    }
}


