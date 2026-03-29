package io.github.juli0mendes.walletmanagement.service;

import io.github.juli0mendes.walletmanagement.dto.*;
import io.github.juli0mendes.walletmanagement.exception.WalletNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletService Unit Tests")
class WalletServiceTest {

    private final WalletService walletService = new WalletService();

    @Test
    @DisplayName("Should create wallet successfully with valid request")
    void shouldCreateWalletSuccessfully() {
        // Given
        var request = new CreateWalletRequest("John Doe", "john@example.com");

        // When
        var response = walletService.createWallet(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getWalletId());
        assertEquals("John Doe", response.getOwnerName());
        assertEquals("john@example.com", response.getOwnerEmail());
        assertEquals(BigDecimal.ZERO, response.getCurrentBalance());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
    }

    @Test
    @DisplayName("Should get wallet successfully when wallet exists")
    void shouldGetWalletSuccessfully() {
        // Given
        var createRequest = new CreateWalletRequest("Jane Smith", "jane@example.com");
        var createdWallet = walletService.createWallet(createRequest);

        // When
        var response = walletService.getWallet(createdWallet.getWalletId());

        // Then
        assertNotNull(response);
        assertEquals(createdWallet.getWalletId(), response.getWalletId());
        assertEquals("Jane Smith", response.getOwnerName());
        assertEquals("jane@example.com", response.getOwnerEmail());
    }

    @Test
    @DisplayName("Should throw WalletNotFoundException when getting non-existent wallet")
    void shouldThrowExceptionWhenGettingNonExistentWallet() {
        // Given
        var nonExistentId = UUID.randomUUID();

        // When & Then
        var exception = assertThrows(WalletNotFoundException.class,
                () -> walletService.getWallet(nonExistentId));

        assertEquals("Wallet not found with id: " + nonExistentId, exception.getMessage());
    }

    @Test
    @DisplayName("Should get current balance successfully")
    void shouldGetCurrentBalanceSuccessfully() {
        // Given
        var createRequest = new CreateWalletRequest("Test User", "test@example.com");
        var createdWallet = walletService.createWallet(createRequest);

        // When
        var response = walletService.getBalance(createdWallet.getWalletId(), null);

        // Then
        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getCurrentBalance());
        assertEquals("USD", response.getCurrency());
        assertNotNull(response.getRetrievedAt());
    }

    @Test
    @DisplayName("Should get balance at specific time successfully")
    void shouldGetBalanceAtSpecificTimeSuccessfully() {
        // Given
        var createRequest = new CreateWalletRequest("Test User", "test@example.com");
        var createdWallet = walletService.createWallet(createRequest);
        var walletId = createdWallet.getWalletId();

        // Deposit some money
        var depositRequest = new DepositRequest(BigDecimal.valueOf(100.00), "Initial deposit");
        walletService.deposit(walletId, depositRequest);

        // When
        var response = walletService.getBalance(walletId, LocalDateTime.now());

        // Then
        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(100.00), response.getCurrentBalance());
    }

    @Test
    @DisplayName("Should throw exception when getting balance of non-existent wallet")
    void shouldThrowExceptionWhenGettingBalanceOfNonExistentWallet() {
        // Given
        var nonExistentId = UUID.randomUUID();

        // When & Then
        var exception = assertThrows(WalletNotFoundException.class,
                () -> walletService.getBalance(nonExistentId, null));

        assertEquals("Wallet not found with id: " + nonExistentId, exception.getMessage());
    }

    @Test
    @DisplayName("Should deposit money successfully")
    void shouldDepositMoneySuccessfully() {
        // Given
        var createRequest = new CreateWalletRequest("Test User", "test@example.com");
        var createdWallet = walletService.createWallet(createRequest);
        var walletId = createdWallet.getWalletId();
        var depositRequest = new DepositRequest(BigDecimal.valueOf(50.00), "Test deposit");

        // When
        var response = walletService.deposit(walletId, depositRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getTransactionId());
        assertEquals(walletId, response.getWalletId());
        assertEquals("DEPOSIT", response.getType());
        assertEquals(BigDecimal.valueOf(50.00), response.getAmount());
        assertEquals(BigDecimal.valueOf(50.00), response.getBalanceAfter());
        assertEquals("Test deposit", response.getDescription());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    @DisplayName("Should throw exception when depositing to non-existent wallet")
    void shouldThrowExceptionWhenDepositingToNonExistentWallet() {
        // Given
        var nonExistentId = UUID.randomUUID();
        var depositRequest = new DepositRequest(BigDecimal.valueOf(25.00), "Test deposit");

        // When & Then
        var exception = assertThrows(WalletNotFoundException.class,
                () -> walletService.deposit(nonExistentId, depositRequest));

        assertEquals("Wallet not found with id: " + nonExistentId, exception.getMessage());
    }

    @Test
    @DisplayName("Should withdraw money successfully")
    void shouldWithdrawMoneySuccessfully() {
        // Given
        var createRequest = new CreateWalletRequest("Test User", "test@example.com");
        var createdWallet = walletService.createWallet(createRequest);
        var walletId = createdWallet.getWalletId();

        // First deposit money
        var depositRequest = new DepositRequest(BigDecimal.valueOf(100.00), "Initial deposit");
        walletService.deposit(walletId, depositRequest);

        // Then withdraw
        var withdrawRequest = new WithdrawRequest(BigDecimal.valueOf(30.00), "Test withdrawal");

        // When
        var response = walletService.withdraw(walletId, withdrawRequest);

        // Then
        assertNotNull(response);
        assertEquals(walletId, response.getWalletId());
        assertEquals("WITHDRAW", response.getType());
        assertEquals(BigDecimal.valueOf(30.00), response.getAmount());
        assertEquals(BigDecimal.valueOf(70.00), response.getBalanceAfter());
        assertEquals("Test withdrawal", response.getDescription());
    }

    @Test
    @DisplayName("Should throw exception when withdrawing from non-existent wallet")
    void shouldThrowExceptionWhenWithdrawingFromNonExistentWallet() {
        // Given
        var nonExistentId = UUID.randomUUID();
        var withdrawRequest = new WithdrawRequest(BigDecimal.valueOf(10.00), "Test withdrawal");

        // When & Then
        var exception = assertThrows(WalletNotFoundException.class,
                () -> walletService.withdraw(nonExistentId, withdrawRequest));

        assertEquals("Wallet not found with id: " + nonExistentId, exception.getMessage());
    }

    @Test
    @DisplayName("Should get transaction history successfully")
    void shouldGetTransactionHistorySuccessfully() {
        // Given
        var createRequest = new CreateWalletRequest("Test User", "test@example.com");
        var createdWallet = walletService.createWallet(createRequest);
        var walletId = createdWallet.getWalletId();

        // Add some transactions
        var depositRequest = new DepositRequest(BigDecimal.valueOf(100.00), "Deposit 1");
        var withdrawRequest = new WithdrawRequest(BigDecimal.valueOf(25.00), "Withdraw 1");

        walletService.deposit(walletId, depositRequest);
        walletService.withdraw(walletId, withdrawRequest);

        // When
        var transactions = walletService.getTransactions(walletId);

        // Then
        assertNotNull(transactions);
        assertEquals(2, transactions.size());

        // Check deposit transaction
        var depositTx = transactions.get(0);
        assertEquals("DEPOSIT", depositTx.getType());
        assertEquals(BigDecimal.valueOf(100.00), depositTx.getAmount());

        // Check withdraw transaction
        var withdrawTx = transactions.get(1);
        assertEquals("WITHDRAW", withdrawTx.getType());
        assertEquals(BigDecimal.valueOf(25.00), withdrawTx.getAmount());
    }

    @Test
    @DisplayName("Should return empty transaction list for wallet with no transactions")
    void shouldReturnEmptyTransactionListForNewWallet() {
        // Given
        var createRequest = new CreateWalletRequest("Test User", "test@example.com");
        var createdWallet = walletService.createWallet(createRequest);

        // When
        var transactions = walletService.getTransactions(createdWallet.getWalletId());

        // Then
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when getting transactions of non-existent wallet")
    void shouldThrowExceptionWhenGettingTransactionsOfNonExistentWallet() {
        // Given
        var nonExistentId = UUID.randomUUID();

        // When & Then
        var exception = assertThrows(WalletNotFoundException.class,
                () -> walletService.getTransactions(nonExistentId));

        assertEquals("Wallet not found with id: " + nonExistentId, exception.getMessage());
    }

    @Test
    @DisplayName("Should calculate balance correctly with multiple transactions")
    void shouldCalculateBalanceCorrectlyWithMultipleTransactions() {
        // Given
        var createRequest = new CreateWalletRequest("Test User", "test@example.com");
        var createdWallet = walletService.createWallet(createRequest);
        var walletId = createdWallet.getWalletId();

        // Multiple transactions
        walletService.deposit(walletId, new DepositRequest(BigDecimal.valueOf(100.00), "Deposit 1"));
        walletService.deposit(walletId, new DepositRequest(BigDecimal.valueOf(50.00), "Deposit 2"));
        walletService.withdraw(walletId, new WithdrawRequest(BigDecimal.valueOf(30.00), "Withdraw 1"));

        // When
        var balanceResponse = walletService.getBalance(walletId, null);

        // Then
        assertEquals(BigDecimal.valueOf(120.00), balanceResponse.getCurrentBalance());
    }

    @Test
    @DisplayName("Should handle insufficient balance for withdrawal")
    void shouldHandleInsufficientBalanceForWithdrawal() {
        // Given
        var createRequest = new CreateWalletRequest("Test User", "test@example.com");
        var createdWallet = walletService.createWallet(createRequest);
        var walletId = createdWallet.getWalletId();

        // Deposit small amount
        var depositRequest = new DepositRequest(BigDecimal.valueOf(50.00), "Small deposit");
        walletService.deposit(walletId, depositRequest);

        // Try to withdraw more than balance
        var withdrawRequest = new WithdrawRequest(BigDecimal.valueOf(100.00), "Large withdrawal");

        // When
        var response = walletService.withdraw(walletId, withdrawRequest);

        // Then - In Stage 1, we allow negative balances (mock behavior)
        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(-50.00), response.getBalanceAfter());
    }

    @Test
    @DisplayName("Should handle zero amount transactions")
    void shouldHandleZeroAmountTransactions() {
        // Given
        var createRequest = new CreateWalletRequest("Test User", "test@example.com");
        var createdWallet = walletService.createWallet(createRequest);
        var walletId = createdWallet.getWalletId();

        // Deposit zero
        var depositRequest = new DepositRequest(BigDecimal.ZERO, "Zero deposit");
        var response = walletService.deposit(walletId, depositRequest);

        // Then
        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getAmount());
        assertEquals(BigDecimal.ZERO, response.getBalanceAfter());
    }

    @Test
    @DisplayName("Should handle very large amounts")
    void shouldHandleVeryLargeAmounts() {
        // Given
        var createRequest = new CreateWalletRequest("Test User", "test@example.com");
        var createdWallet = walletService.createWallet(createRequest);
        var walletId = createdWallet.getWalletId();

        // Very large amount
        var largeAmount = new BigDecimal("999999999999.99");
        var depositRequest = new DepositRequest(largeAmount, "Large deposit");

        // When
        var response = walletService.deposit(walletId, depositRequest);

        // Then
        assertNotNull(response);
        assertEquals(largeAmount, response.getAmount());
        assertEquals(largeAmount, response.getBalanceAfter());
    }

    @Test
    @DisplayName("Should handle multiple rapid transactions")
    void shouldHandleMultipleRapidTransactions() {
        // Given
        var createRequest = new CreateWalletRequest("Test User", "test@example.com");
        var createdWallet = walletService.createWallet(createRequest);
        var walletId = createdWallet.getWalletId();

        // Multiple rapid transactions
        for (int i = 1; i <= 10; i++) {
            var depositRequest = new DepositRequest(BigDecimal.valueOf(i), "Deposit " + i);
            walletService.deposit(walletId, depositRequest);
        }

        // When
        var balanceResponse = walletService.getBalance(walletId, null);

        // Then
        assertEquals(BigDecimal.valueOf(55), balanceResponse.getCurrentBalance()); // Sum 1-10 = 55
    }

    @Test
    @DisplayName("Should handle transactions with special characters in description")
    void shouldHandleTransactionsWithSpecialCharactersInDescription() {
        // Given
        var createRequest = new CreateWalletRequest("Test User", "test@example.com");
        var createdWallet = walletService.createWallet(createRequest);
        var walletId = createdWallet.getWalletId();

        // Description with special characters
        var specialDescription = "Deposit with émojis 🎉 and spëcial chärs";
        var depositRequest = new DepositRequest(BigDecimal.valueOf(25.00), specialDescription);

        // When
        var response = walletService.deposit(walletId, depositRequest);

        // Then
        assertNotNull(response);
        assertEquals(specialDescription, response.getDescription());
    }

    @Test
    @DisplayName("Should handle balance query with future timestamp")
    void shouldHandleBalanceQueryWithFutureTimestamp() {
        // Given
        var createRequest = new CreateWalletRequest("Test User", "test@example.com");
        var createdWallet = walletService.createWallet(createRequest);
        var walletId = createdWallet.getWalletId();

        // Future timestamp
        var futureTime = LocalDateTime.now().plusDays(1);

        // When
        var response = walletService.getBalance(walletId, futureTime);

        // Then
        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getCurrentBalance()); // No transactions in future
    }

    @Test
    @DisplayName("Should handle balance query with very old timestamp")
    void shouldHandleBalanceQueryWithVeryOldTimestamp() {
        // Given
        var createRequest = new CreateWalletRequest("Test User", "test@example.com");
        var createdWallet = walletService.createWallet(createRequest);
        var walletId = createdWallet.getWalletId();

        // Very old timestamp
        var oldTime = LocalDateTime.now().minusYears(10);

        // When
        var response = walletService.getBalance(walletId, oldTime);

        // Then
        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getCurrentBalance()); // No transactions in past
    }

    @Test
    @DisplayName("Should handle wallet creation with maximum length names")
    void shouldHandleWalletCreationWithMaximumLengthNames() {
        // Given - Very long names
        var longName = "A".repeat(1000);
        var longEmail = "a".repeat(100) + "@example.com";
        var request = new CreateWalletRequest(longName, longEmail);

        // When
        var response = walletService.createWallet(request);

        // Then
        assertNotNull(response);
        assertEquals(longName, response.getOwnerName());
        assertEquals(longEmail, response.getOwnerEmail());
    }

    @Test
    @DisplayName("Should handle wallet creation with unicode characters")
    void shouldHandleWalletCreationWithUnicodeCharacters() {
        // Given
        var unicodeName = "José María ñoño 🚀";
        var unicodeEmail = "tëst@ëxämplé.com";
        var request = new CreateWalletRequest(unicodeName, unicodeEmail);

        // When
        var response = walletService.createWallet(request);

        // Then
        assertNotNull(response);
        assertEquals(unicodeName, response.getOwnerName());
        assertEquals(unicodeEmail, response.getOwnerEmail());
    }

    @Test
    @DisplayName("Should handle empty description in transactions")
    void shouldHandleEmptyDescriptionInTransactions() {
        // Given
        var createRequest = new CreateWalletRequest("Test User", "test@example.com");
        var createdWallet = walletService.createWallet(createRequest);
        var walletId = createdWallet.getWalletId();

        // Empty description
        var depositRequest = new DepositRequest(BigDecimal.valueOf(10.00), "");

        // When
        var response = walletService.deposit(walletId, depositRequest);

        // Then
        assertNotNull(response);
        assertEquals("", response.getDescription());
    }

    @Test
    @DisplayName("Should handle null description in transactions")
    void shouldHandleNullDescriptionInTransactions() {
        // Given
        var createRequest = new CreateWalletRequest("Test User", "test@example.com");
        var createdWallet = walletService.createWallet(createRequest);
        var walletId = createdWallet.getWalletId();

        // Null description
        var depositRequest = new DepositRequest(BigDecimal.valueOf(10.00), null);

        // When
        var response = walletService.deposit(walletId, depositRequest);

        // Then
        assertNotNull(response);
        assertNull(response.getDescription());
    }
}

