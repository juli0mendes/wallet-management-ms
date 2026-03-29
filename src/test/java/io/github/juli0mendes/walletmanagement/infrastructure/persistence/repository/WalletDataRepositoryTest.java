package io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository;

import io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.WalletDocument;
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
@DisplayName("WalletDataRepository Integration Tests with Embedded MongoDB")
class WalletDataRepositoryTest {

    @Autowired
    private WalletDataRepository walletDataRepository;

    @BeforeEach
    void setUp() {
        walletDataRepository.deleteAll();
    }

    private WalletDocument buildWalletDocument() {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        return new WalletDocument(id, "John Doe", "john@example.com", BigDecimal.ZERO, now, now);
    }

    @Test
    @DisplayName("Should save wallet document to embedded MongoDB")
    void shouldSaveWalletDocument() {
        WalletDocument document = buildWalletDocument();

        WalletDocument saved = walletDataRepository.save(document);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(document.getOwnerName(), saved.getOwnerName());
        assertEquals(document.getOwnerEmail(), saved.getOwnerEmail());
    }

    @Test
    @DisplayName("Should find wallet document by ID from embedded MongoDB")
    void shouldFindWalletDocumentById() {
        WalletDocument document = buildWalletDocument();
        walletDataRepository.save(document);

        Optional<WalletDocument> found = walletDataRepository.findById(document.getId());

        assertTrue(found.isPresent());
        assertEquals(document.getId(), found.get().getId());
        assertEquals(document.getOwnerName(), found.get().getOwnerName());
    }

    @Test
    @DisplayName("Should return empty when wallet document not found")
    void shouldReturnEmptyWhenWalletNotFound() {
        String nonExistentId = UUID.randomUUID().toString();

        Optional<WalletDocument> found = walletDataRepository.findById(nonExistentId);

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should find all wallet documents from embedded MongoDB")
    void shouldFindAllWalletDocuments() {
        WalletDocument doc1 = buildWalletDocument();
        WalletDocument doc2 = buildWalletDocument();
        walletDataRepository.save(doc1);
        walletDataRepository.save(doc2);

        List<WalletDocument> result = walletDataRepository.findAll();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should return empty list when no wallet documents exist")
    void shouldReturnEmptyListWhenNoWalletsExist() {
        List<WalletDocument> result = walletDataRepository.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should update wallet document in embedded MongoDB")
    void shouldUpdateWalletDocument() {
        WalletDocument document = buildWalletDocument();
        WalletDocument saved = walletDataRepository.save(document);

        saved.setBalance(new BigDecimal("100.00"));
        saved.setUpdatedAt(LocalDateTime.now());
        walletDataRepository.save(saved);

        Optional<WalletDocument> updated = walletDataRepository.findById(saved.getId());
        assertTrue(updated.isPresent());
        assertEquals(new BigDecimal("100.00"), updated.get().getBalance());
    }

    @Test
    @DisplayName("Should delete wallet document by ID from embedded MongoDB")
    void shouldDeleteWalletDocument() {
        WalletDocument document = buildWalletDocument();
        WalletDocument saved = walletDataRepository.save(document);

        walletDataRepository.deleteById(saved.getId());

        Optional<WalletDocument> deleted = walletDataRepository.findById(saved.getId());
        assertTrue(deleted.isEmpty());
    }

    @Test
    @DisplayName("Should not throw error when deleting non-existent wallet document")
    void shouldNotThrowErrorWhenDeletingNonExistent() {
        String nonExistentId = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> walletDataRepository.deleteById(nonExistentId));
    }
}

