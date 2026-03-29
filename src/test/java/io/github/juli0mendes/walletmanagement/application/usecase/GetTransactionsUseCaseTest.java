package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.TransactionType;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.TransactionRepository;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.dto.TransactionResponse;
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
@DisplayName("GetTransactionsUseCase Unit Tests")
class GetTransactionsUseCaseTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private GetTransactionsUseCase getTransactionsUseCase;

    @Test
    @DisplayName("Should get transactions successfully")
    void shouldGetTransactionsSuccessfully() {
        var walletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var wallet = new Wallet(walletId, "John Doe", "john@example.com", BigDecimal.valueOf(150.00), now, now);
        var transactions = List.of(
                new Transaction(UUID.randomUUID(), walletId, TransactionType.DEPOSIT, BigDecimal.valueOf(100.00), BigDecimal.valueOf(100.00), "Deposit", now),
                new Transaction(UUID.randomUUID(), walletId, TransactionType.WITHDRAW, BigDecimal.valueOf(50.00), BigDecimal.valueOf(50.00), "Withdraw", now)
        );

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletId(walletId)).thenReturn(transactions);

        List<TransactionResponse> responses = getTransactionsUseCase.execute(walletId);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("DEPOSIT", responses.get(0).type());
        assertEquals("WITHDRAW", responses.get(1).type());
    }

    @Test
    @DisplayName("Should throw WalletNotFoundException when wallet not found")
    void shouldThrowWalletNotFoundExceptionWhenWalletNotFound() {
        var walletId = UUID.randomUUID();

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> getTransactionsUseCase.execute(walletId));
    }
}
