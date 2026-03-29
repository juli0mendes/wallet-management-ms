package io.github.juli0mendes.walletmanagement.domain.repository;

import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {
    Optional<Wallet> findById(UUID id);
    Wallet save(Wallet wallet);
    List<Transaction> findTransactionsByWalletId(UUID walletId);
    List<Transaction> findTransactionsByWalletIdAndCreatedAtBefore(UUID walletId, LocalDateTime at);
    Transaction saveTransaction(Transaction transaction);
}
