package io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository;

import io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransactionDocument;
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
@DisplayName("TransactionDataRepository Integration Tests with Embedded MongoDB")
class TransactionDataRepositoryTest {

    @Autowired
    private TransactionDataRepository transactionDataRepository;

    @BeforeEach
    void setUp() {
        transactionDataRepository.deleteAll();
    }

    private TransactionDocument buildTransactionDocument(String walletId) {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        return new TransactionDocument(id, walletId, "DEPOSIT", new BigDecimal("100.00"),
                new BigDecimal("100.00"), "Initial deposit", now);
    }

    @Test
    @DisplayName("Should save transaction document to embedded MongoDB")
    void shouldSaveTransactionDocument() {
        String walletId = UUID.randomUUID().toString();
        TransactionDocument document = buildTransactionDocument(walletId);

        TransactionDocument saved = transactionDataRepository.save(document);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(document.getWalletId(), saved.getWalletId());
        assertEquals(document.getType(), saved.getType());
    }

    @Test
    @DisplayName("Should find transaction document by ID from embedded MongoDB")
    void shouldFindTransactionDocumentById() {
        String walletId = UUID.randomUUID().toString();
        TransactionDocument document = buildTransactionDocument(walletId);
        transactionDataRepository.save(document);

        Optional<TransactionDocument> found = transactionDataRepository.findById(document.getId());

        assertTrue(found.isPresent());
        assertEquals(document.getId(), found.get().getId());
        assertEquals(document.getWalletId(), found.get().getWalletId());
    }

    @Test
    @DisplayName("Should return empty when transaction document not found")
    void shouldReturnEmptyWhenTransactionNotFound() {
        String nonExistentId = UUID.randomUUID().toString();

        Optional<TransactionDocument> found = transactionDataRepository.findById(nonExistentId);

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should find all transaction documents from embedded MongoDB")
    void shouldFindAllTransactionDocuments() {
        String walletId = UUID.randomUUID().toString();
        TransactionDocument doc1 = buildTransactionDocument(walletId);
        TransactionDocument doc2 = buildTransactionDocument(walletId);
        transactionDataRepository.save(doc1);
        transactionDataRepository.save(doc2);

        List<TransactionDocument> result = transactionDataRepository.findAll();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should return empty list when no transaction documents exist")
    void shouldReturnEmptyListWhenNoTransactionsExist() {
        List<TransactionDocument> result = transactionDataRepository.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find transactions by wallet ID from embedded MongoDB")
    void shouldFindTransactionsByWalletId() {
        String walletId = UUID.randomUUID().toString();
        String otherWalletId = UUID.randomUUID().toString();

        TransactionDocument doc1 = buildTransactionDocument(walletId);
        TransactionDocument doc2 = buildTransactionDocument(walletId);
        TransactionDocument doc3 = buildTransactionDocument(otherWalletId);

        transactionDataRepository.save(doc1);
        transactionDataRepository.save(doc2);
        transactionDataRepository.save(doc3);

        List<TransactionDocument> result = transactionDataRepository.findByWalletId(walletId);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(tx -> tx.getWalletId().equals(walletId)));
    }

    @Test
    @DisplayName("Should return empty list when no transactions found for wallet ID")
    void shouldReturnEmptyListWhenNoTransactionsForWalletId() {
        String walletId = UUID.randomUUID().toString();

        List<TransactionDocument> result = transactionDataRepository.findByWalletId(walletId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find transactions by wallet ID and date filter from embedded MongoDB")
    void shouldFindTransactionsByWalletIdAndCreatedAtBefore() {
        String walletId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        TransactionDocument oldTx = new TransactionDocument(
                UUID.randomUUID().toString(), walletId, "DEPOSIT", new BigDecimal("100.00"),
                new BigDecimal("100.00"), "Old transaction", now.minusHours(2));
        TransactionDocument recentTx = new TransactionDocument(
                UUID.randomUUID().toString(), walletId, "WITHDRAW", new BigDecimal("50.00"),
                new BigDecimal("50.00"), "Recent transaction", now);

        transactionDataRepository.save(oldTx);
        transactionDataRepository.save(recentTx);

        LocalDateTime cutoffTime = now.minusHours(1);
        List<TransactionDocument> result = transactionDataRepository
                .findByWalletIdAndCreatedAtLessThanEqual(walletId, cutoffTime);

        assertEquals(1, result.size());
        assertEquals(oldTx.getId(), result.getFirst().getId());
    }

    @Test
    @DisplayName("Should return empty list when no transactions found before specified date")
    void shouldReturnEmptyListWhenNoTransactionsBeforeDate() {
        String walletId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        TransactionDocument tx = buildTransactionDocument(walletId);
        tx.setCreatedAt(now);
        transactionDataRepository.save(tx);

        LocalDateTime cutoffTime = now.minusHours(2);
        List<TransactionDocument> result = transactionDataRepository
                .findByWalletIdAndCreatedAtLessThanEqual(walletId, cutoffTime);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should delete transaction document by ID from embedded MongoDB")
    void shouldDeleteTransactionDocument() {
        String walletId = UUID.randomUUID().toString();
        TransactionDocument document = buildTransactionDocument(walletId);
        TransactionDocument saved = transactionDataRepository.save(document);

        transactionDataRepository.deleteById(saved.getId());

        Optional<TransactionDocument> deleted = transactionDataRepository.findById(saved.getId());
        assertTrue(deleted.isEmpty());
    }

    @Test
    @DisplayName("Should not throw error when deleting non-existent transaction document")
    void shouldNotThrowErrorWhenDeletingNonExistent() {
        String nonExistentId = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> transactionDataRepository.deleteById(nonExistentId));
    }
}


