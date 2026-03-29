package io.github.juli0mendes.walletmanagement.infrastructure.persistence.document;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionDocument Unit Tests")
class TransactionDocumentTest {

    private static final String ID = "tx-123";
    private static final String WALLET_ID = "wallet-456";
    private static final String TYPE = "DEPOSIT";
    private static final BigDecimal AMOUNT = new BigDecimal("100.00");
    private static final BigDecimal BALANCE_AFTER = new BigDecimal("250.00");
    private static final String DESCRIPTION = "Test deposit";
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    @DisplayName("Should create TransactionDocument with default constructor and all fields null")
    void shouldCreateTransactionDocumentWithDefaultConstructor() {
        TransactionDocument doc = new TransactionDocument();

        assertNull(doc.getId());
        assertNull(doc.getWalletId());
        assertNull(doc.getType());
        assertNull(doc.getAmount());
        assertNull(doc.getBalanceAfter());
        assertNull(doc.getDescription());
        assertNull(doc.getCreatedAt());
    }

    @Test
    @DisplayName("Should create TransactionDocument with parameterized constructor")
    void shouldCreateTransactionDocumentWithParameterizedConstructor() {
        TransactionDocument doc = new TransactionDocument(ID, WALLET_ID, TYPE, AMOUNT, BALANCE_AFTER, DESCRIPTION, NOW);

        assertEquals(ID, doc.getId());
        assertEquals(WALLET_ID, doc.getWalletId());
        assertEquals(TYPE, doc.getType());
        assertEquals(AMOUNT, doc.getAmount());
        assertEquals(BALANCE_AFTER, doc.getBalanceAfter());
        assertEquals(DESCRIPTION, doc.getDescription());
        assertEquals(NOW, doc.getCreatedAt());
    }

    @Test
    @DisplayName("Should set and get id")
    void shouldSetAndGetId() {
        TransactionDocument doc = new TransactionDocument();
        doc.setId(ID);
        assertEquals(ID, doc.getId());
    }

    @Test
    @DisplayName("Should set and get walletId")
    void shouldSetAndGetWalletId() {
        TransactionDocument doc = new TransactionDocument();
        doc.setWalletId(WALLET_ID);
        assertEquals(WALLET_ID, doc.getWalletId());
    }

    @Test
    @DisplayName("Should set and get type")
    void shouldSetAndGetType() {
        TransactionDocument doc = new TransactionDocument();
        doc.setType(TYPE);
        assertEquals(TYPE, doc.getType());
    }

    @Test
    @DisplayName("Should set and get amount")
    void shouldSetAndGetAmount() {
        TransactionDocument doc = new TransactionDocument();
        doc.setAmount(AMOUNT);
        assertEquals(AMOUNT, doc.getAmount());
    }

    @Test
    @DisplayName("Should set and get balanceAfter")
    void shouldSetAndGetBalanceAfter() {
        TransactionDocument doc = new TransactionDocument();
        doc.setBalanceAfter(BALANCE_AFTER);
        assertEquals(BALANCE_AFTER, doc.getBalanceAfter());
    }

    @Test
    @DisplayName("Should set and get description")
    void shouldSetAndGetDescription() {
        TransactionDocument doc = new TransactionDocument();
        doc.setDescription(DESCRIPTION);
        assertEquals(DESCRIPTION, doc.getDescription());
    }

    @Test
    @DisplayName("Should set and get createdAt")
    void shouldSetAndGetCreatedAt() {
        TransactionDocument doc = new TransactionDocument();
        doc.setCreatedAt(NOW);
        assertEquals(NOW, doc.getCreatedAt());
    }

    @Test
    @DisplayName("Should overwrite field values via setters")
    void shouldOverwriteFieldValuesViaSetters() {
        TransactionDocument doc = new TransactionDocument(ID, WALLET_ID, TYPE, AMOUNT, BALANCE_AFTER, DESCRIPTION, NOW);

        String newId = "tx-999";
        String newWalletId = "wallet-999";
        String newType = "WITHDRAW";
        BigDecimal newAmount = new BigDecimal("50.00");
        BigDecimal newBalance = new BigDecimal("200.00");
        String newDesc = "Updated desc";
        LocalDateTime later = NOW.plusHours(1);

        doc.setId(newId);
        doc.setWalletId(newWalletId);
        doc.setType(newType);
        doc.setAmount(newAmount);
        doc.setBalanceAfter(newBalance);
        doc.setDescription(newDesc);
        doc.setCreatedAt(later);

        assertEquals(newId, doc.getId());
        assertEquals(newWalletId, doc.getWalletId());
        assertEquals(newType, doc.getType());
        assertEquals(newAmount, doc.getAmount());
        assertEquals(newBalance, doc.getBalanceAfter());
        assertEquals(newDesc, doc.getDescription());
        assertEquals(later, doc.getCreatedAt());
    }

    @Test
    @DisplayName("Should accept null values via setters")
    void shouldAcceptNullValuesViaSetters() {
        TransactionDocument doc = new TransactionDocument(ID, WALLET_ID, TYPE, AMOUNT, BALANCE_AFTER, DESCRIPTION, NOW);

        doc.setId(null);
        doc.setWalletId(null);
        doc.setType(null);
        doc.setAmount(null);
        doc.setBalanceAfter(null);
        doc.setDescription(null);
        doc.setCreatedAt(null);

        assertNull(doc.getId());
        assertNull(doc.getWalletId());
        assertNull(doc.getType());
        assertNull(doc.getAmount());
        assertNull(doc.getBalanceAfter());
        assertNull(doc.getDescription());
        assertNull(doc.getCreatedAt());
    }

    @Test
    @DisplayName("Should accept null description in parameterized constructor")
    void shouldAcceptNullDescriptionInConstructor() {
        TransactionDocument doc = new TransactionDocument(ID, WALLET_ID, TYPE, AMOUNT, BALANCE_AFTER, null, NOW);

        assertNull(doc.getDescription());
        assertEquals(AMOUNT, doc.getAmount());
    }

    @Test
    @DisplayName("Should accept all transaction types")
    void shouldAcceptAllTransactionTypes() {
        for (String type : new String[]{"DEPOSIT", "WITHDRAW", "TRANSFER_IN", "TRANSFER_OUT"}) {
            TransactionDocument doc = new TransactionDocument(ID, WALLET_ID, type, AMOUNT, BALANCE_AFTER, DESCRIPTION, NOW);
            assertEquals(type, doc.getType());
        }
    }
}

