package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.dto.TransferRequest;
import io.github.juli0mendes.walletmanagement.dto.TransferResponse;
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
@DisplayName("TransferUseCase Unit Tests")
class TransferUseCaseTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private TransferUseCase transferUseCase;

    @Test
    @DisplayName("Should transfer successfully")
    void shouldTransferSuccessfully() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var sourceWallet = new Wallet(sourceWalletId, "John Doe", "john@example.com", BigDecimal.valueOf(200.00), now, now);
        var destWallet = new Wallet(destinationWalletId, "Jane Doe", "jane@example.com", BigDecimal.valueOf(50.00), now, now);
        var request = new TransferRequest(sourceWalletId, destinationWalletId, BigDecimal.valueOf(100.00), "Test transfer");

        when(walletRepository.findById(sourceWalletId)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(destinationWalletId)).thenReturn(Optional.of(destWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(sourceWallet, destWallet); // First call for source, second for dest
        when(walletRepository.saveTransaction(any(Transaction.class))).thenReturn(new Transaction(UUID.randomUUID(), sourceWalletId, "TRANSFER_OUT", BigDecimal.valueOf(100.00), BigDecimal.valueOf(100.00), "Test transfer", now));

        TransferResponse response = transferUseCase.execute(request);

        assertNotNull(response);
        assertEquals(sourceWalletId, response.getSourceWalletId());
        assertEquals(destinationWalletId, response.getDestinationWalletId());
        assertEquals(BigDecimal.valueOf(100.00), response.getAmount());
        assertEquals("COMPLETED", response.getStatus());
    }

    @Test
    @DisplayName("Should throw WalletNotFoundException when source wallet not found")
    void shouldThrowWalletNotFoundExceptionWhenSourceWalletNotFound() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var request = new TransferRequest(sourceWalletId, destinationWalletId, BigDecimal.valueOf(100.00), "Test transfer");

        when(walletRepository.findById(sourceWalletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> transferUseCase.execute(request));
    }

    @Test
    @DisplayName("Should throw WalletNotFoundException when destination wallet not found")
    void shouldThrowWalletNotFoundExceptionWhenDestinationWalletNotFound() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var sourceWallet = new Wallet(sourceWalletId, "John Doe", "john@example.com", BigDecimal.valueOf(200.00), now, now);
        var request = new TransferRequest(sourceWalletId, destinationWalletId, BigDecimal.valueOf(100.00), "Test transfer");

        when(walletRepository.findById(sourceWalletId)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(destinationWalletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> transferUseCase.execute(request));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when insufficient balance")
    void shouldThrowIllegalArgumentExceptionWhenInsufficientBalance() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var sourceWallet = new Wallet(sourceWalletId, "John Doe", "john@example.com", BigDecimal.valueOf(50.00), now, now);
        var destWallet = new Wallet(destinationWalletId, "Jane Doe", "jane@example.com", BigDecimal.valueOf(50.00), now, now);
        var request = new TransferRequest(sourceWalletId, destinationWalletId, BigDecimal.valueOf(100.00), "Test transfer");

        when(walletRepository.findById(sourceWalletId)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(destinationWalletId)).thenReturn(Optional.of(destWallet));

        assertThrows(IllegalArgumentException.class, () -> transferUseCase.execute(request));
    }
}
