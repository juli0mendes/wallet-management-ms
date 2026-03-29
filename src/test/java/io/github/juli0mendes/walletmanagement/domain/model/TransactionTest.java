package io.github.juli0mendes.walletmanagement.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Transaction Domain Model Unit Tests")
class TransactionTest {

    @Test
    @DisplayName("Should create transaction successfully")
    void shouldCreateTransactionSuccessfully() {
        var id = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var now = LocalDateTime.now();

        Transaction transaction = new Transaction(id, walletId, TransactionType.DEPOSIT, BigDecimal.valueOf(50.00), BigDecimal.valueOf(150.00), "Test deposit", now);

        assertEquals(id, transaction.getId());
        assertEquals(walletId, transaction.getWalletId());
        assertEquals(TransactionType.DEPOSIT, transaction.getType());
        assertEquals(BigDecimal.valueOf(50.00), transaction.getAmount());
        assertEquals(BigDecimal.valueOf(150.00), transaction.getBalanceAfter());
        assertEquals("Test deposit", transaction.getDescription());
        assertEquals(now, transaction.getCreatedAt());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null id")
    void shouldThrowIllegalArgumentExceptionForNullId() {
        var walletId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(null, walletId, TransactionType.DEPOSIT, BigDecimal.valueOf(50.00), BigDecimal.valueOf(150.00), "Test", LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null wallet id")
    void shouldThrowIllegalArgumentExceptionForNullWalletId() {
        var id = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(id, null, TransactionType.DEPOSIT, BigDecimal.valueOf(50.00), BigDecimal.valueOf(150.00), "Test", LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null type")
    void shouldThrowIllegalArgumentExceptionForNullType() {
        var id = UUID.randomUUID();
        var walletId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(id, walletId, null, BigDecimal.valueOf(50.00), BigDecimal.valueOf(150.00), "Test", LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null amount")
    void shouldThrowIllegalArgumentExceptionForNullAmount() {
        var id = UUID.randomUUID();
        var walletId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(id, walletId, TransactionType.DEPOSIT, null, BigDecimal.valueOf(150.00), "Test", LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for zero amount")
    void shouldThrowIllegalArgumentExceptionForZeroAmount() {
        var id = UUID.randomUUID();
        var walletId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(id, walletId, TransactionType.DEPOSIT, BigDecimal.ZERO, BigDecimal.valueOf(150.00), "Test", LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for negative amount")
    void shouldThrowIllegalArgumentExceptionForNegativeAmount() {
        var id = UUID.randomUUID();
        var walletId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(id, walletId, TransactionType.DEPOSIT, BigDecimal.valueOf(-50.00), BigDecimal.valueOf(150.00), "Test", LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null balance after")
    void shouldThrowIllegalArgumentExceptionForNullBalanceAfter() {
        var id = UUID.randomUUID();
        var walletId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(id, walletId, TransactionType.DEPOSIT, BigDecimal.valueOf(50.00), null, "Test", LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for negative balance after")
    void shouldThrowIllegalArgumentExceptionForNegativeBalanceAfter() {
        var id = UUID.randomUUID();
        var walletId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(id, walletId, TransactionType.DEPOSIT, BigDecimal.valueOf(50.00), BigDecimal.valueOf(-10.00), "Test", LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null created at")
    void shouldThrowIllegalArgumentExceptionForNullCreatedAt() {
        var id = UUID.randomUUID();
        var walletId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(id, walletId, TransactionType.DEPOSIT, BigDecimal.valueOf(50.00), BigDecimal.valueOf(150.00), "Test", null)
        );
    }
}
