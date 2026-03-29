package io.github.juli0mendes.walletmanagement.service;

import io.github.juli0mendes.walletmanagement.dto.*;
import io.github.juli0mendes.walletmanagement.exception.WalletNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class WalletService {

    private final Map<UUID, WalletData> walletStore = new HashMap<>();
    private final Map<UUID, List<TransactionData>> transactionStore = new HashMap<>();

    public WalletResponse createWallet(CreateWalletRequest request) {
        UUID walletId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        WalletData walletData = new WalletData(
                walletId,
                request.getOwnerName(),
                request.getOwnerEmail(),
                BigDecimal.ZERO,
                now,
                now
        );

        walletStore.put(walletId, walletData);
        transactionStore.put(walletId, new ArrayList<>());

        return mapToResponse(walletData);
    }

    public WalletResponse getWallet(UUID walletId) {
        WalletData walletData = findWalletOrThrow(walletId);
        return mapToResponse(walletData);
    }

    public BalanceResponse getBalance(UUID walletId, LocalDateTime at) {
        WalletData walletData = findWalletOrThrow(walletId);
        
        BigDecimal balance = walletData.getCurrentBalance();
        
        if (at != null) {
            balance = calculateBalanceAtTime(walletId, at);
        }

        return new BalanceResponse(balance, "USD", LocalDateTime.now());
    }

    public List<TransactionResponse> getTransactions(UUID walletId) {
        findWalletOrThrow(walletId);
        
        List<TransactionResponse> transactions = new ArrayList<>();
        List<TransactionData> walletTransactions = transactionStore.getOrDefault(walletId, new ArrayList<>());

        for (TransactionData txData : walletTransactions) {
            transactions.add(mapTransactionToResponse(txData));
        }

        return transactions;
    }

    public TransactionResponse deposit(UUID walletId, DepositRequest request) {
        WalletData walletData = findWalletOrThrow(walletId);
        
        BigDecimal newBalance = walletData.getCurrentBalance().add(request.getAmount());
        walletData.setCurrentBalance(newBalance);
        walletData.setUpdatedAt(LocalDateTime.now());

        TransactionData transactionData = new TransactionData(
                UUID.randomUUID(),
                walletId,
                "DEPOSIT",
                request.getAmount(),
                newBalance,
                request.getDescription(),
                LocalDateTime.now()
        );

        transactionStore.get(walletId).add(transactionData);

        return mapTransactionToResponse(transactionData);
    }

    public TransactionResponse withdraw(UUID walletId, WithdrawRequest request) {
        WalletData walletData = findWalletOrThrow(walletId);
        
        BigDecimal newBalance = walletData.getCurrentBalance().subtract(request.getAmount());
        walletData.setCurrentBalance(newBalance);
        walletData.setUpdatedAt(LocalDateTime.now());

        TransactionData transactionData = new TransactionData(
                UUID.randomUUID(),
                walletId,
                "WITHDRAW",
                request.getAmount(),
                newBalance,
                request.getDescription(),
                LocalDateTime.now()
        );

        transactionStore.get(walletId).add(transactionData);

        return mapTransactionToResponse(transactionData);
    }

    private WalletData findWalletOrThrow(UUID walletId) {
        WalletData wallet = walletStore.get(walletId);
        if (wallet == null) {
            throw new WalletNotFoundException(walletId.toString());
        }
        return wallet;
    }

    private BigDecimal calculateBalanceAtTime(UUID walletId, LocalDateTime at) {
        List<TransactionData> transactions = transactionStore.getOrDefault(walletId, new ArrayList<>());
        BigDecimal balance = BigDecimal.ZERO;

        for (TransactionData tx : transactions) {
            if (tx.getCreatedAt().isBefore(at) || tx.getCreatedAt().isEqual(at)) {
                balance = tx.getBalanceAfter();
            }
        }

        return balance;
    }

    private WalletResponse mapToResponse(WalletData walletData) {
        return new WalletResponse(
                walletData.getWalletId(),
                walletData.getOwnerName(),
                walletData.getOwnerEmail(),
                walletData.getCurrentBalance(),
                walletData.getCreatedAt(),
                walletData.getUpdatedAt()
        );
    }

    private TransactionResponse mapTransactionToResponse(TransactionData txData) {
        return new TransactionResponse(
                txData.getTransactionId(),
                txData.getWalletId(),
                txData.getType(),
                txData.getAmount(),
                txData.getBalanceAfter(),
                txData.getDescription(),
                txData.getCreatedAt()
        );
    }

    // Inner classes for mocked data storage
    private static class WalletData {
        private UUID walletId;
        private String ownerName;
        private String ownerEmail;
        private BigDecimal currentBalance;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public WalletData(UUID walletId, String ownerName, String ownerEmail,
                         BigDecimal currentBalance, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.walletId = walletId;
            this.ownerName = ownerName;
            this.ownerEmail = ownerEmail;
            this.currentBalance = currentBalance;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public UUID getWalletId() { return walletId; }
        public String getOwnerName() { return ownerName; }
        public String getOwnerEmail() { return ownerEmail; }
        public BigDecimal getCurrentBalance() { return currentBalance; }
        public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    private static class TransactionData {
        private UUID transactionId;
        private UUID walletId;
        private String type;
        private BigDecimal amount;
        private BigDecimal balanceAfter;
        private String description;
        private LocalDateTime createdAt;

        public TransactionData(UUID transactionId, UUID walletId, String type, BigDecimal amount,
                              BigDecimal balanceAfter, String description, LocalDateTime createdAt) {
            this.transactionId = transactionId;
            this.walletId = walletId;
            this.type = type;
            this.amount = amount;
            this.balanceAfter = balanceAfter;
            this.description = description;
            this.createdAt = createdAt;
        }

        public UUID getTransactionId() { return transactionId; }
        public UUID getWalletId() { return walletId; }
        public String getType() { return type; }
        public BigDecimal getAmount() { return amount; }
        public BigDecimal getBalanceAfter() { return balanceAfter; }
        public String getDescription() { return description; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}


