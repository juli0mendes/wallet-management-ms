package io.github.juli0mendes.walletmanagement.infrastructure.persistence.document;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WalletDocument Unit Tests")
class WalletDocumentTest {

    private static final String ID = "wallet-123";
    private static final String OWNER_NAME = "John Doe";
    private static final String OWNER_EMAIL = "john@example.com";
    private static final BigDecimal BALANCE = new BigDecimal("100.00");
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    @DisplayName("Should create WalletDocument with default constructor and all fields null")
    void shouldCreateWalletDocumentWithDefaultConstructor() {
        WalletDocument doc = new WalletDocument();

        assertNull(doc.getId());
        assertNull(doc.getOwnerName());
        assertNull(doc.getOwnerEmail());
        assertNull(doc.getBalance());
        assertNull(doc.getCreatedAt());
        assertNull(doc.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create WalletDocument with parameterized constructor")
    void shouldCreateWalletDocumentWithParameterizedConstructor() {
        WalletDocument doc = new WalletDocument(ID, OWNER_NAME, OWNER_EMAIL, BALANCE, NOW, NOW);

        assertEquals(ID, doc.getId());
        assertEquals(OWNER_NAME, doc.getOwnerName());
        assertEquals(OWNER_EMAIL, doc.getOwnerEmail());
        assertEquals(BALANCE, doc.getBalance());
        assertEquals(NOW, doc.getCreatedAt());
        assertEquals(NOW, doc.getUpdatedAt());
    }

    @Test
    @DisplayName("Should set and get id")
    void shouldSetAndGetId() {
        WalletDocument doc = new WalletDocument();
        doc.setId(ID);
        assertEquals(ID, doc.getId());
    }

    @Test
    @DisplayName("Should set and get ownerName")
    void shouldSetAndGetOwnerName() {
        WalletDocument doc = new WalletDocument();
        doc.setOwnerName(OWNER_NAME);
        assertEquals(OWNER_NAME, doc.getOwnerName());
    }

    @Test
    @DisplayName("Should set and get ownerEmail")
    void shouldSetAndGetOwnerEmail() {
        WalletDocument doc = new WalletDocument();
        doc.setOwnerEmail(OWNER_EMAIL);
        assertEquals(OWNER_EMAIL, doc.getOwnerEmail());
    }

    @Test
    @DisplayName("Should set and get balance")
    void shouldSetAndGetBalance() {
        WalletDocument doc = new WalletDocument();
        doc.setBalance(BALANCE);
        assertEquals(BALANCE, doc.getBalance());
    }

    @Test
    @DisplayName("Should set and get createdAt")
    void shouldSetAndGetCreatedAt() {
        WalletDocument doc = new WalletDocument();
        doc.setCreatedAt(NOW);
        assertEquals(NOW, doc.getCreatedAt());
    }

    @Test
    @DisplayName("Should set and get updatedAt")
    void shouldSetAndGetUpdatedAt() {
        WalletDocument doc = new WalletDocument();
        doc.setUpdatedAt(NOW);
        assertEquals(NOW, doc.getUpdatedAt());
    }

    @Test
    @DisplayName("Should overwrite field values via setters")
    void shouldOverwriteFieldValuesViaSetters() {
        WalletDocument doc = new WalletDocument(ID, OWNER_NAME, OWNER_EMAIL, BALANCE, NOW, NOW);

        String newId = "wallet-999";
        String newName = "Jane Doe";
        String newEmail = "jane@example.com";
        BigDecimal newBalance = new BigDecimal("250.00");
        LocalDateTime later = NOW.plusDays(1);

        doc.setId(newId);
        doc.setOwnerName(newName);
        doc.setOwnerEmail(newEmail);
        doc.setBalance(newBalance);
        doc.setCreatedAt(later);
        doc.setUpdatedAt(later);

        assertEquals(newId, doc.getId());
        assertEquals(newName, doc.getOwnerName());
        assertEquals(newEmail, doc.getOwnerEmail());
        assertEquals(newBalance, doc.getBalance());
        assertEquals(later, doc.getCreatedAt());
        assertEquals(later, doc.getUpdatedAt());
    }

    @Test
    @DisplayName("Should accept null values via setters")
    void shouldAcceptNullValuesViaSetters() {
        WalletDocument doc = new WalletDocument(ID, OWNER_NAME, OWNER_EMAIL, BALANCE, NOW, NOW);

        doc.setId(null);
        doc.setOwnerName(null);
        doc.setOwnerEmail(null);
        doc.setBalance(null);
        doc.setCreatedAt(null);
        doc.setUpdatedAt(null);

        assertNull(doc.getId());
        assertNull(doc.getOwnerName());
        assertNull(doc.getOwnerEmail());
        assertNull(doc.getBalance());
        assertNull(doc.getCreatedAt());
        assertNull(doc.getUpdatedAt());
    }

    @Test
    @DisplayName("Should accept zero balance")
    void shouldAcceptZeroBalance() {
        WalletDocument doc = new WalletDocument(ID, OWNER_NAME, OWNER_EMAIL, BigDecimal.ZERO, NOW, NOW);
        assertEquals(BigDecimal.ZERO, doc.getBalance());
    }

    @Test
    @DisplayName("Should accept different createdAt and updatedAt")
    void shouldAcceptDifferentCreatedAtAndUpdatedAt() {
        LocalDateTime createdAt = NOW.minusDays(5);
        LocalDateTime updatedAt = NOW;

        WalletDocument doc = new WalletDocument(ID, OWNER_NAME, OWNER_EMAIL, BALANCE, createdAt, updatedAt);

        assertEquals(createdAt, doc.getCreatedAt());
        assertEquals(updatedAt, doc.getUpdatedAt());
        assertTrue(doc.getUpdatedAt().isAfter(doc.getCreatedAt()));
    }
}

