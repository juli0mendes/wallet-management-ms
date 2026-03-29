package io.github.juli0mendes.walletmanagement.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Wallet Domain Model Unit Tests")
class WalletTest {

    @Test
    @DisplayName("Should create wallet successfully with valid parameters")
    void shouldCreateWalletSuccessfully() {
        var id = UUID.randomUUID();
        var ownerName = "John Doe";
        var ownerEmail = "john@example.com";
        var balance = BigDecimal.valueOf(100.00);
        var now = LocalDateTime.now();

        var wallet = new Wallet(id, ownerName, ownerEmail, balance, now, now);

        assertEquals(id, wallet.getId());
        assertEquals(ownerName, wallet.getOwnerName());
        assertEquals(ownerEmail, wallet.getOwnerEmail());
        assertEquals(balance, wallet.getBalance());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when id is null")
    void shouldThrowIllegalArgumentExceptionWhenIdIsNull() {
        var now = LocalDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> 
            new Wallet(null, "John", "john@example.com", BigDecimal.valueOf(100.00), now, now)
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when owner name is null")
    void shouldThrowIllegalArgumentExceptionWhenOwnerNameIsNull() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> 
            new Wallet(id, null, "john@example.com", BigDecimal.valueOf(100.00), now, now)
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when owner name is blank")
    void shouldThrowIllegalArgumentExceptionWhenOwnerNameIsBlank() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> 
            new Wallet(id, "   ", "john@example.com", BigDecimal.valueOf(100.00), now, now)
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when owner email is null")
    void shouldThrowIllegalArgumentExceptionWhenOwnerEmailIsNull() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> 
            new Wallet(id, "John", null, BigDecimal.valueOf(100.00), now, now)
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when owner email is blank")
    void shouldThrowIllegalArgumentExceptionWhenOwnerEmailIsBlank() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> 
            new Wallet(id, "John", "   ", BigDecimal.valueOf(100.00), now, now)
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when balance is null")
    void shouldThrowIllegalArgumentExceptionWhenBalanceIsNull() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> 
            new Wallet(id, "John", "john@example.com", null, now, now)
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when balance is negative")
    void shouldThrowIllegalArgumentExceptionWhenBalanceIsNegative() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> 
            new Wallet(id, "John", "john@example.com", BigDecimal.valueOf(-50.00), now, now)
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when createdAt is null")
    void shouldThrowIllegalArgumentExceptionWhenCreatedAtIsNull() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> 
            new Wallet(id, "John", "john@example.com", BigDecimal.valueOf(100.00), null, now)
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updatedAt is null")
    void shouldThrowIllegalArgumentExceptionWhenUpdatedAtIsNull() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> 
            new Wallet(id, "John", "john@example.com", BigDecimal.valueOf(100.00), now, null)
        );
    }

    @Test
    @DisplayName("Should deposit successfully")
    void shouldDepositSuccessfully() {
        var now = LocalDateTime.now();
        var wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", BigDecimal.valueOf(100.00), now, now);

        wallet.deposit(BigDecimal.valueOf(50.00));

        assertEquals(BigDecimal.valueOf(150.00), wallet.getBalance());
        assertNotNull(wallet.getUpdatedAt());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null deposit amount")
    void shouldThrowIllegalArgumentExceptionForNullDepositAmount() {
        var now = LocalDateTime.now();
        var wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", BigDecimal.valueOf(100.00), now, now);

        assertThrows(IllegalArgumentException.class, () -> wallet.deposit(null));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for zero deposit amount")
    void shouldThrowIllegalArgumentExceptionForZeroDepositAmount() {
        var now = LocalDateTime.now();
        var wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", BigDecimal.valueOf(100.00), now, now);

        assertThrows(IllegalArgumentException.class, () -> wallet.deposit(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for negative deposit amount")
    void shouldThrowIllegalArgumentExceptionForNegativeDepositAmount() {
        var now = LocalDateTime.now();
        var wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", BigDecimal.valueOf(100.00), now, now);

        assertThrows(IllegalArgumentException.class, () -> wallet.deposit(BigDecimal.valueOf(-10.00)));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null withdraw amount")
    void shouldThrowIllegalArgumentExceptionForNullWithdrawAmount() {
        var now = LocalDateTime.now();
        var wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", BigDecimal.valueOf(100.00), now, now);

        assertThrows(IllegalArgumentException.class, () -> wallet.withdraw(null));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for zero withdraw amount")
    void shouldThrowIllegalArgumentExceptionForZeroWithdrawAmount() {
        var now = LocalDateTime.now();
        var wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", BigDecimal.valueOf(100.00), now, now);

        assertThrows(IllegalArgumentException.class, () -> wallet.withdraw(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for insufficient balance")
    void shouldThrowIllegalArgumentExceptionForInsufficientBalance() {
        var now = LocalDateTime.now();
        var wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", BigDecimal.valueOf(50.00), now, now);

        assertThrows(IllegalArgumentException.class, () -> wallet.withdraw(BigDecimal.valueOf(100.00)));
    }
}
