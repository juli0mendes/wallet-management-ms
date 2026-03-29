package io.github.juli0mendes.walletmanagement.infrastructure.repository;

import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryWalletRepository Unit Tests")
class InMemoryWalletRepositoryTest {

    private InMemoryWalletRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryWalletRepository();
    }

    @Test
    @DisplayName("Should save and find wallet by id")
    void shouldSaveAndFindWalletById() {
        var walletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var wallet = new Wallet(walletId, "John Doe", "john@example.com", BigDecimal.valueOf(100.00), now, now);

        Wallet saved = repository.save(wallet);
        Optional<Wallet> found = repository.findById(walletId);

        assertEquals(wallet, saved);
        assertTrue(found.isPresent());
        assertEquals(wallet, found.get());
    }

    @Test
    @DisplayName("Should return empty when wallet not found")
    void shouldReturnEmptyWhenWalletNotFound() {
        var walletId = UUID.randomUUID();

        Optional<Wallet> found = repository.findById(walletId);

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should save and find transactions by wallet id")
    void shouldSaveAndFindTransactionsByWalletId() {
        var walletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var transaction = new Transaction(UUID.randomUUID(), walletId, "DEPOSIT", BigDecimal.valueOf(50.00), BigDecimal.valueOf(50.00), "Deposit", now);

        repository.saveTransaction(transaction);
        List<Transaction> transactions = repository.findTransactionsByWalletId(walletId);

        assertEquals(1, transactions.size());
        assertEquals(transaction, transactions.get(0));
    }

    @Test
    @DisplayName("Should find transactions by wallet id and created at before")
    void shouldFindTransactionsByWalletIdAndCreatedAtBefore() {
        var walletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var at = now.minusHours(1);
        var transaction1 = new Transaction(UUID.randomUUID(), walletId, "DEPOSIT", BigDecimal.valueOf(50.00), BigDecimal.valueOf(50.00), "Deposit", at.minusHours(1));
        var transaction2 = new Transaction(UUID.randomUUID(), walletId, "WITHDRAW", BigDecimal.valueOf(20.00), BigDecimal.valueOf(30.00), "Withdraw", at.plusHours(1));

        repository.saveTransaction(transaction1);
        repository.saveTransaction(transaction2);
        List<Transaction> transactions = repository.findTransactionsByWalletIdAndCreatedAtBefore(walletId, at);

        assertEquals(1, transactions.size());
        assertEquals(transaction1, transactions.get(0));
    }
}
