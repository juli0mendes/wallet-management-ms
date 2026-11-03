package io.github.juli0mendes.walletmanagement.domain.repository;

import io.github.juli0mendes.walletmanagement.domain.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository {
    List<Transaction> findByWalletId(UUID walletId);
    List<Transaction> findByWalletIdAndCreatedAtBefore(UUID walletId, LocalDateTime at);
    List<Transaction> findByTransferId(UUID transferId);
    Transaction save(Transaction transaction);
}

