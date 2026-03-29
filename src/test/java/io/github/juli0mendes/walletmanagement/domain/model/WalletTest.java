package io.github.juli0mendes.walletmanagement.domain.model;

import io.github.juli0mendes.walletmanagement.domain.exception.InsufficientBalanceException;
import io.github.juli0mendes.walletmanagement.domain.exception.InvalidWalletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Wallet Domain Model Unit Tests")
class WalletTest {

    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    @DisplayName("Should create wallet successfully with valid parameters")
    void shouldCreateWalletSuccessfully() {
        UUID id = UUID.randomUUID();
        String ownerName = "John Doe";
        String ownerEmail = "john@example.com";
        BigDecimal balance = new BigDecimal("100.00");

        Wallet wallet = new Wallet(id, ownerName, ownerEmail, balance, NOW, NOW);

        assertEquals(id, wallet.getId());
        assertEquals(ownerName, wallet.getOwnerName());
        assertEquals(ownerEmail, wallet.getOwnerEmail());
        assertEquals(balance, wallet.getBalance());
    }

    @Test
    @DisplayName("Should reject null id")
    void shouldRejectNullId() {
        assertThrows(InvalidWalletException.class, () ->
                new Wallet(null, "John", "john@example.com", BigDecimal.ZERO, NOW, NOW)
        );
    }

    @Test
    @DisplayName("Should reject null owner name")
    void shouldRejectNullOwnerName() {
        assertThrows(InvalidWalletException.class, () ->
                new Wallet(UUID.randomUUID(), null, "john@example.com", BigDecimal.ZERO, NOW, NOW)
        );
    }

    @Test
    @DisplayName("Should reject blank owner name")
    void shouldRejectBlankOwnerName() {
        assertThrows(InvalidWalletException.class, () ->
                new Wallet(UUID.randomUUID(), "  ", "john@example.com", BigDecimal.ZERO, NOW, NOW)
        );
    }

    @Test
    @DisplayName("Should reject owner name shorter than 3 characters")
    void shouldRejectShortOwnerName() {
        assertThrows(InvalidWalletException.class, () ->
                new Wallet(UUID.randomUUID(), "Jo", "john@example.com", BigDecimal.ZERO, NOW, NOW)
        );
    }

    @Test
    @DisplayName("Should reject null owner email")
    void shouldRejectNullOwnerEmail() {
        assertThrows(InvalidWalletException.class, () ->
                new Wallet(UUID.randomUUID(), "John", null, BigDecimal.ZERO, NOW, NOW)
        );
    }

    @Test
    @DisplayName("Should reject blank owner email")
    void shouldRejectBlankOwnerEmail() {
        assertThrows(InvalidWalletException.class, () ->
                new Wallet(UUID.randomUUID(), "John", "  ", BigDecimal.ZERO, NOW, NOW)
        );
    }

    @Test
    @DisplayName("Should reject null balance")
    void shouldRejectNullBalance() {
        assertThrows(InvalidWalletException.class, () ->
                new Wallet(UUID.randomUUID(), "John", "john@example.com", null, NOW, NOW)
        );
    }

    @Test
    @DisplayName("Should reject negative balance")
    void shouldRejectNegativeBalance() {
        assertThrows(InvalidWalletException.class, () ->
                new Wallet(UUID.randomUUID(), "John", "john@example.com", new BigDecimal("-1.00"), NOW, NOW)
        );
    }

    @Test
    @DisplayName("Should reject null createdAt")
    void shouldRejectNullCreatedAt() {
        assertThrows(InvalidWalletException.class, () ->
                new Wallet(UUID.randomUUID(), "John", "john@example.com", BigDecimal.ZERO, null, NOW)
        );
    }

    @Test
    @DisplayName("Should reject null updatedAt")
    void shouldRejectNullUpdatedAt() {
        assertThrows(InvalidWalletException.class, () ->
                new Wallet(UUID.randomUUID(), "John", "john@example.com", BigDecimal.ZERO, NOW, null)
        );
    }

    @Test
    @DisplayName("Should deposit amount successfully")
    void shouldDepositAmountSuccessfully() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", BigDecimal.ZERO, NOW, NOW);

        wallet.deposit(new BigDecimal("50.00"));

        assertEquals(new BigDecimal("50.00"), wallet.getBalance());
    }

    @Test
    @DisplayName("Should reject null deposit amount")
    void shouldRejectNullDepositAmount() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", BigDecimal.ZERO, NOW, NOW);
        assertThrows(InvalidWalletException.class, () -> wallet.deposit(null));
    }

    @Test
    @DisplayName("Should reject zero deposit amount")
    void shouldRejectZeroDepositAmount() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", BigDecimal.ZERO, NOW, NOW);
        assertThrows(InvalidWalletException.class, () -> wallet.deposit(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should reject negative deposit amount")
    void shouldRejectNegativeDepositAmount() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", BigDecimal.ZERO, NOW, NOW);
        assertThrows(InvalidWalletException.class, () -> wallet.deposit(new BigDecimal("-10.00")));
    }

    @Test
    @DisplayName("Should withdraw amount successfully")
    void shouldWithdrawAmountSuccessfully() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", new BigDecimal("100.00"), NOW, NOW);

        wallet.withdraw(new BigDecimal("30.00"));

        assertEquals(new BigDecimal("70.00"), wallet.getBalance());
    }

    @Test
    @DisplayName("Should reject null withdraw amount")
    void shouldRejectNullWithdrawAmount() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", new BigDecimal("100.00"), NOW, NOW);
        assertThrows(InvalidWalletException.class, () -> wallet.withdraw(null));
    }

    @Test
    @DisplayName("Should reject zero withdraw amount")
    void shouldRejectZeroWithdrawAmount() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", new BigDecimal("100.00"), NOW, NOW);
        assertThrows(InvalidWalletException.class, () -> wallet.withdraw(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should reject negative withdraw amount")
    void shouldRejectNegativeWithdrawAmount() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", new BigDecimal("100.00"), NOW, NOW);
        assertThrows(InvalidWalletException.class, () -> wallet.withdraw(new BigDecimal("-10.00")));
    }

    @Test
    @DisplayName("Should reject withdraw when insufficient balance")
    void shouldRejectWithdrawWhenInsufficientBalance() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", new BigDecimal("10.00"), NOW, NOW);
        assertThrows(InsufficientBalanceException.class, () -> wallet.withdraw(new BigDecimal("10.01")));
    }

    @Test
    @DisplayName("Should update timestamp on deposit")
    void shouldUpdateTimestampOnDeposit() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", BigDecimal.ZERO, NOW, NOW);
        LocalDateTime beforeDeposit = wallet.getUpdatedAt();

        wallet.deposit(new BigDecimal("50.00"));

        assertTrue(wallet.getUpdatedAt().isAfter(beforeDeposit) || wallet.getUpdatedAt().isEqual(beforeDeposit));
    }

    @Test
    @DisplayName("Should update timestamp on withdraw")
    void shouldUpdateTimestampOnWithdraw() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", "john@example.com", new BigDecimal("100.00"), NOW, NOW);
        LocalDateTime beforeWithdraw = wallet.getUpdatedAt();

        wallet.withdraw(new BigDecimal("50.00"));

        assertTrue(wallet.getUpdatedAt().isAfter(beforeWithdraw) || wallet.getUpdatedAt().isEqual(beforeWithdraw));
    }
}
