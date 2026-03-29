package com.walletmanagement.domain.entity;

import com.walletmanagement.domain.exception.InsufficientBalanceException;
import com.walletmanagement.domain.exception.InvalidWalletException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WalletTest {

    @Test
    void shouldCreateWalletWithValidData() {
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        Wallet wallet = new Wallet(id, "John Doe", BigDecimal.TEN, createdAt);

        assertEquals(id, wallet.getId());
        assertEquals("John Doe", wallet.getOwnerName());
        assertEquals(BigDecimal.TEN, wallet.getBalance());
        assertEquals(createdAt, wallet.getCreatedAt());
    }

    @Test
    void shouldCreateWalletFactoryWithDefaultBalance() {
        Wallet wallet = Wallet.create("Alice");

        assertEquals("Alice", wallet.getOwnerName());
        assertEquals(BigDecimal.ZERO, wallet.getBalance());
    }

    @Test
    void shouldRejectInvalidOwnerName() {
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        assertThrows(InvalidWalletException.class, () -> new Wallet(id, null, BigDecimal.ZERO, createdAt));
        assertThrows(InvalidWalletException.class, () -> new Wallet(id, " ", BigDecimal.ZERO, createdAt));
        assertThrows(InvalidWalletException.class, () -> new Wallet(id, "ab", BigDecimal.ZERO, createdAt));
    }

    @Test
    void shouldRejectNegativeBalance() {
        assertThrows(
                InvalidWalletException.class,
                () -> new Wallet(UUID.randomUUID(), "John", new BigDecimal("-1.00"), LocalDateTime.now())
        );
    }

    @Test
    void shouldDepositAmount() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", BigDecimal.ZERO, LocalDateTime.now());

        wallet.deposit(new BigDecimal("50.00"));

        assertEquals(new BigDecimal("50.00"), wallet.getBalance());
    }

    @Test
    void shouldRejectInvalidDeposit() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", BigDecimal.ZERO, LocalDateTime.now());

        assertThrows(InvalidWalletException.class, () -> wallet.deposit(null));
        assertThrows(InvalidWalletException.class, () -> wallet.deposit(BigDecimal.ZERO));
        assertThrows(InvalidWalletException.class, () -> wallet.deposit(new BigDecimal("-2.00")));
    }

    @Test
    void shouldWithdrawAmount() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", new BigDecimal("100.00"), LocalDateTime.now());

        wallet.withdraw(new BigDecimal("35.00"));

        assertEquals(new BigDecimal("65.00"), wallet.getBalance());
    }

    @Test
    void shouldRejectInvalidWithdraw() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", new BigDecimal("100.00"), LocalDateTime.now());

        assertThrows(InvalidWalletException.class, () -> wallet.withdraw(null));
        assertThrows(InvalidWalletException.class, () -> wallet.withdraw(BigDecimal.ZERO));
        assertThrows(InvalidWalletException.class, () -> wallet.withdraw(new BigDecimal("-1.00")));
    }

    @Test
    void shouldRejectWithdrawWhenInsufficientBalance() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", new BigDecimal("10.00"), LocalDateTime.now());

        assertThrows(InsufficientBalanceException.class, () -> wallet.withdraw(new BigDecimal("10.01")));
    }
}

