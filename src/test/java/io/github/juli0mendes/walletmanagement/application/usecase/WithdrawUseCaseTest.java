package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.TransactionType;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.dto.TransactionResponse;
import io.github.juli0mendes.walletmanagement.dto.WithdrawRequest;
import io.github.juli0mendes.walletmanagement.exception.WalletNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawUseCase Unit Tests")
class WithdrawUseCaseTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WithdrawUseCase withdrawUseCase;

    @Test
    @DisplayName("Should withdraw successfully")
    void shouldWithdrawSuccessfully() {
        var walletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var wallet = new Wallet(walletId, "John Doe", "john@example.com", BigDecimal.valueOf(100.00), now, now);
        var updatedWallet = new Wallet(walletId, "John Doe", "john@example.com", BigDecimal.valueOf(50.00), now, now.plusSeconds(1));
        var request = new WithdrawRequest(BigDecimal.valueOf(50.00), "Test withdraw");
        var transaction = new Transaction(UUID.randomUUID(), walletId, TransactionType.WITHDRAW, BigDecimal.valueOf(50.00), BigDecimal.valueOf(50.00), "Test withdraw", now.plusSeconds(1));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(updatedWallet);
        when(walletRepository.saveTransaction(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = withdrawUseCase.execute(walletId, request);

        assertNotNull(response);
        assertEquals(walletId, response.getWalletId());
        assertEquals("WITHDRAW", response.getType());
        assertEquals(BigDecimal.valueOf(50.00), response.getAmount());
        assertEquals(BigDecimal.valueOf(50.00), response.getBalanceAfter());
    }

    @Test
    @DisplayName("Should throw WalletNotFoundException when wallet not found")
    void shouldThrowWalletNotFoundExceptionWhenWalletNotFound() {
        var walletId = UUID.randomUUID();
        var request = new WithdrawRequest(BigDecimal.valueOf(50.00), "Test withdraw");

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> withdrawUseCase.execute(walletId, request));
    }
}
