package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.TransactionType;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.dto.BalanceResponse;
import io.github.juli0mendes.walletmanagement.exception.WalletNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetBalanceUseCase Unit Tests")
class GetBalanceUseCaseTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private GetBalanceUseCase getBalanceUseCase;

    @Test
    @DisplayName("Should get current balance successfully")
    void shouldGetCurrentBalanceSuccessfully() {
        var walletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var wallet = new Wallet(walletId, "John Doe", "john@example.com", BigDecimal.valueOf(150.00), now, now);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        BalanceResponse response = getBalanceUseCase.execute(walletId, null);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(150.00), response.getCurrentBalance());
        assertEquals("USD", response.getCurrency());
    }

    @Test
    @DisplayName("Should get historical balance successfully")
    void shouldGetHistoricalBalanceSuccessfully() {
        var walletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var at = now.minusDays(1);
        var wallet = new Wallet(walletId, "John Doe", "john@example.com", BigDecimal.valueOf(150.00), now, now);
        var transactions = List.of(
                new Transaction(UUID.randomUUID(), walletId, TransactionType.DEPOSIT, BigDecimal.valueOf(100.00), BigDecimal.valueOf(100.00), "Deposit", at.minusHours(1)),
                new Transaction(UUID.randomUUID(), walletId, TransactionType.DEPOSIT, BigDecimal.valueOf(50.00), BigDecimal.valueOf(150.00), "Deposit", at.plusHours(1))
        );

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.findTransactionsByWalletIdAndCreatedAtBefore(walletId, at)).thenReturn(transactions.subList(0, 1));

        BalanceResponse response = getBalanceUseCase.execute(walletId, at);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(100.00), response.getCurrentBalance());
    }

    @Test
    @DisplayName("Should throw WalletNotFoundException when wallet not found")
    void shouldThrowWalletNotFoundExceptionWhenWalletNotFound() {
        var walletId = UUID.randomUUID();

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> getBalanceUseCase.execute(walletId, null));
    }
}
