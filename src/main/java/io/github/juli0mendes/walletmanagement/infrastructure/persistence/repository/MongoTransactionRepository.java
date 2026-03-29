package io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository;

import io.github.juli0mendes.walletmanagement.domain.exception.InvalidWalletException;
import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.repository.TransactionRepository;
import io.github.juli0mendes.walletmanagement.infrastructure.persistence.mapper.TransactionMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class MongoTransactionRepository implements TransactionRepository {

    private final TransactionDataRepository transactionDataRepository;
    private final TransactionMapper transactionMapper;

    public MongoTransactionRepository(
            TransactionDataRepository transactionDataRepository,
            TransactionMapper transactionMapper) {
        this.transactionDataRepository = transactionDataRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public List<Transaction> findByWalletId(UUID walletId) {
        validateId(walletId);
        return transactionDataRepository.findByWalletId(walletId.toString())
                .stream()
                .map(transactionMapper::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> findByWalletIdAndCreatedAtBefore(UUID walletId, LocalDateTime at) {
        validateId(walletId);
        if (at == null) {
            throw new InvalidWalletException("Reference date cannot be null");
        }
        return transactionDataRepository.findByWalletIdAndCreatedAtLessThanEqual(walletId.toString(), at)
                .stream()
                .map(transactionMapper::toDomain)
                .toList();
    }

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction == null) {
            throw new InvalidWalletException("Transaction cannot be null");
        }
        return transactionMapper.toDomain(transactionDataRepository.save(transactionMapper.toDocument(transaction)));
    }

    private void validateId(UUID id) {
        if (id == null) {
            throw new InvalidWalletException("Id cannot be null");
        }
    }
}

