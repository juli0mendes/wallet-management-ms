package io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository;

import io.github.juli0mendes.walletmanagement.domain.exception.InvalidWalletException;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.infrastructure.persistence.mapper.WalletMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@DisplayName("MongoWalletRepository Integration Tests with Embedded MongoDB")
class MongoWalletRepositoryTest {

    @Autowired
    private WalletDataRepository walletDataRepository;

    private MongoWalletRepository mongoWalletRepository;
    private WalletMapper walletMapper;

    @BeforeEach
    void setUp() {
        walletDataRepository.deleteAll();

        walletMapper = new WalletMapper();
        mongoWalletRepository = new MongoWalletRepository(walletDataRepository, walletMapper);
    }

    private Wallet buildWallet(String ownerName, String ownerEmail) {
        LocalDateTime now = LocalDateTime.now();
        return new Wallet(UUID.randomUUID(), ownerName, ownerEmail, BigDecimal.ZERO, now, now);
    }

    @Test
    @DisplayName("Should save and retrieve wallet from embedded MongoDB")
    void shouldSaveAndRetrieveWallet() {
        Wallet wallet = buildWallet("John Doe", "john@example.com");

        Wallet saved = mongoWalletRepository.save(wallet);
        Optional<Wallet> retrieved = mongoWalletRepository.findById(wallet.getId());

        assertTrue(retrieved.isPresent());
        assertEquals(saved.getId(), retrieved.get().getId());
        assertEquals(saved.getOwnerName(), retrieved.get().getOwnerName());
        assertEquals(saved.getOwnerEmail(), retrieved.get().getOwnerEmail());
    }

    @Test
    @DisplayName("Should return empty when wallet not found in embedded MongoDB")
    void shouldReturnEmptyWhenWalletNotFound() {
        UUID id = UUID.randomUUID();
        Optional<Wallet> result = mongoWalletRepository.findById(id);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find all wallets from embedded MongoDB")
    void shouldFindAllWallets() {
        Wallet w1 = buildWallet("Alice", "alice@example.com");
        Wallet w2 = buildWallet("Bob", "bob@example.com");

        mongoWalletRepository.save(w1);
        mongoWalletRepository.save(w2);

        List<Wallet> result = mongoWalletRepository.findAll();
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should delete wallet by ID from embedded MongoDB")
    void shouldDeleteWalletById() {
        Wallet wallet = buildWallet("John", "john@example.com");
        mongoWalletRepository.save(wallet);

        mongoWalletRepository.deleteById(wallet.getId());

        assertTrue(mongoWalletRepository.findById(wallet.getId()).isEmpty());
    }


    @Test
    @DisplayName("Should reject null wallet in embedded MongoDB operation")
    void shouldRejectNullWallet() {
        assertThrows(InvalidWalletException.class, () -> mongoWalletRepository.save(null));
    }

    @Test
    @DisplayName("Should reject null wallet ID")
    void shouldRejectNullWalletId() {
        assertThrows(InvalidWalletException.class, () -> mongoWalletRepository.findById(null));
        assertThrows(InvalidWalletException.class, () -> mongoWalletRepository.deleteById(null));
    }

    @Test
    @DisplayName("Should update wallet balance persistence")
    void shouldUpdateWalletBalancePersistence() {
        Wallet wallet = buildWallet("John", "john@example.com");
        Wallet saved = mongoWalletRepository.save(wallet);

        saved.deposit(new BigDecimal("100.00"));
        mongoWalletRepository.save(saved);

        Optional<Wallet> retrieved = mongoWalletRepository.findById(wallet.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(new BigDecimal("100.00"), retrieved.get().getBalance());
    }
}

