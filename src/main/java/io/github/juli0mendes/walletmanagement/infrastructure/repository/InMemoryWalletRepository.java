package io.github.juli0mendes.walletmanagement.infrastructure.repository;

import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryWalletRepository implements WalletRepository {

    private final Map<UUID, Wallet> walletStore = new HashMap<>();
    private final Map<UUID, List<Transaction>> transactionStore = new HashMap<>();

    @Override
    public Optional<Wallet> findById(UUID id) {
        return Optional.ofNullable(walletStore.get(id));
    }

    @Override
    public Wallet save(Wallet wallet) {
        walletStore.put(wallet.getId(), wallet);
        if (!transactionStore.containsKey(wallet.getId())) {
            transactionStore.put(wallet.getId(), new ArrayList<>());
        }
        return wallet;
    }

    @Override
    public List<Transaction> findTransactionsByWalletId(UUID walletId) {
        return transactionStore.getOrDefault(walletId, new ArrayList<>());
    }

    @Override
    public List<Transaction> findTransactionsByWalletIdAndCreatedAtBefore(UUID walletId, LocalDateTime at) {
        return transactionStore.getOrDefault(walletId, new ArrayList<>()).stream()
                .filter(tx -> tx.getCreatedAt().isBefore(at) || tx.getCreatedAt().isEqual(at))
                .collect(Collectors.toList());
    }

    @Override
    public Transaction saveTransaction(Transaction transaction) {
        transactionStore.computeIfAbsent(transaction.getWalletId(), k -> new ArrayList<>()).add(transaction);
        return transaction;
    }
}
