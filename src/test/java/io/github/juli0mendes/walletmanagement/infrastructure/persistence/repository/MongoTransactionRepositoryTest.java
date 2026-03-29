package io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository;

import io.github.juli0mendes.walletmanagement.domain.exception.InvalidWalletException;
import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.TransactionType;
import io.github.juli0mendes.walletmanagement.infrastructure.persistence.mapper.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataMongoTest
@DisplayName("MongoTransactionRepository Integration Tests with Embedded MongoDB")
class MongoTransactionRepositoryTest {

    @Autowired
    private TransactionDataRepository transactionDataRepository;

    private MongoTransactionRepository mongoTransactionRepository;

    @BeforeEach
    void setUp() {
        transactionDataRepository.deleteAll();
        mongoTransactionRepository = new MongoTransactionRepository(transactionDataRepository, new TransactionMapper());
    }

    private Transaction buildTransaction(UUID walletId, TransactionType type, BigDecimal amount, BigDecimal balanceAfter, LocalDateTime createdAt) {
        return new Transaction(UUID.randomUUID(), walletId, type, amount, balanceAfter, "desc", createdAt);
    }

    @Test
    @DisplayName("Should save and retrieve transaction in embedded MongoDB")
    void shouldSaveAndRetrieveTransaction() {
        UUID walletId = UUID.randomUUID();
        Transaction transaction = buildTransaction(walletId, TransactionType.DEPOSIT,
                new BigDecimal("50.00"), new BigDecimal("50.00"), LocalDateTime.now());

        mongoTransactionRepository.save(transaction);

        List<Transaction> retrieved = mongoTransactionRepository.findByWalletId(walletId);
        assertEquals(1, retrieved.size());
        assertEquals(transaction.getId(), retrieved.get(0).getId());
    }

    @Test
    @DisplayName("Should find transactions by wallet and date from embedded MongoDB")
    void shouldFindTransactionsByWalletAndDate() {
        UUID walletId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Transaction tx1 = buildTransaction(walletId, TransactionType.DEPOSIT,
                new BigDecimal("100.00"), new BigDecimal("100.00"), now.minusHours(2));
        Transaction tx2 = buildTransaction(walletId, TransactionType.WITHDRAW,
                new BigDecimal("50.00"), new BigDecimal("50.00"), now);

        mongoTransactionRepository.save(tx1);
        mongoTransactionRepository.save(tx2);

        List<Transaction> result = mongoTransactionRepository
                .findByWalletIdAndCreatedAtBefore(walletId, now.minusHours(1));

        assertEquals(1, result.size());
        assertEquals(tx1.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Should reject null transaction")
    void shouldRejectNullTransaction() {
        assertThrows(InvalidWalletException.class, () -> mongoTransactionRepository.save(null));
    }

    @Test
    @DisplayName("Should reject null wallet ID")
    void shouldRejectNullWalletId() {
        assertThrows(InvalidWalletException.class, () -> mongoTransactionRepository.findByWalletId(null));
    }
}

