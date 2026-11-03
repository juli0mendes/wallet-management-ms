package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.exception.DuplicateTransferOperationException;
import io.github.juli0mendes.walletmanagement.domain.exception.InsufficientBalanceException;
import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.TransactionType;
import io.github.juli0mendes.walletmanagement.domain.model.TransferOperation;
import io.github.juli0mendes.walletmanagement.domain.model.TransferOperationStatus;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.TransactionRepository;
import io.github.juli0mendes.walletmanagement.domain.repository.TransferOperationRepository;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransferOperationRepository transferOperationRepository;

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
        var request = new TransferRequest(sourceWalletId, destinationWalletId, BigDecimal.valueOf(100.00), "Test transfer", "op-success-1");

        when(transferOperationRepository.findByOperationId("op-success-1")).thenReturn(Optional.empty());
        when(walletRepository.findById(sourceWalletId)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(destinationWalletId)).thenReturn(Optional.of(destWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(sourceWallet, destWallet);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction(UUID.randomUUID(), sourceWalletId, TransactionType.TRANSFER_OUT, BigDecimal.valueOf(100.00), BigDecimal.valueOf(100.00), "Test transfer", now));

        TransferResponse response = transferUseCase.execute(request);

        assertNotNull(response);
        assertEquals(sourceWalletId, response.getSourceWalletId());
        assertEquals(destinationWalletId, response.getDestinationWalletId());
        assertEquals(BigDecimal.valueOf(100.00), response.getAmount());
        assertEquals("COMPLETED", response.getStatus());
    }

    @Test
    @DisplayName("Should return the original result for the same operation id")
    void shouldReturnOriginalResultForSameOperationId() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var request = new TransferRequest(sourceWalletId, destinationWalletId, BigDecimal.valueOf(100.00), "Test transfer", "op-duplicate-safe");
        var operation = new TransferOperation(
                UUID.randomUUID(),
                "op-duplicate-safe",
                payloadHashOf(request),
                TransferOperationStatus.APPLIED,
                "Test transfer",
                now,
                now
        );

        when(transferOperationRepository.findByOperationId("op-duplicate-safe")).thenReturn(Optional.of(operation));

        var response = transferUseCase.execute(request);

        assertEquals("COMPLETED", response.getStatus());
        assertEquals("op-duplicate-safe", response.getOperationId());
    }

    @Test
    @DisplayName("Should reject a reused operation id with a different payload")
    void shouldRejectReusedOperationIdWithDifferentPayload() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var originalRequest = new TransferRequest(sourceWalletId, destinationWalletId, BigDecimal.valueOf(100.00), "Original transfer", "op-conflict");
        var operation = new TransferOperation(
                UUID.randomUUID(),
                "op-conflict",
                payloadHashOf(originalRequest),
                TransferOperationStatus.APPLIED,
                "Original transfer",
                now,
                now
        );
        var request = new TransferRequest(sourceWalletId, destinationWalletId, BigDecimal.valueOf(100.00), "Different transfer", "op-conflict");

        when(transferOperationRepository.findByOperationId("op-conflict")).thenReturn(Optional.of(operation));

        assertThrows(DuplicateTransferOperationException.class, () -> transferUseCase.execute(request));
    }

    @Test
    @DisplayName("Should throw WalletNotFoundException when source wallet not found")
    void shouldThrowWalletNotFoundExceptionWhenSourceWalletNotFound() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var request = new TransferRequest(sourceWalletId, destinationWalletId, BigDecimal.valueOf(100.00), "Test transfer", "op-source-missing");

        when(transferOperationRepository.findByOperationId("op-source-missing")).thenReturn(Optional.empty());
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
        var request = new TransferRequest(sourceWalletId, destinationWalletId, BigDecimal.valueOf(100.00), "Test transfer", "op-destination-missing");

        when(transferOperationRepository.findByOperationId("op-destination-missing")).thenReturn(Optional.empty());
        when(walletRepository.findById(sourceWalletId)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(destinationWalletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> transferUseCase.execute(request));
    }

    @Test
    @DisplayName("Should throw InsufficientBalanceException when insufficient balance")
    void shouldThrowInsufficientBalanceExceptionWhenInsufficientBalance() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var sourceWallet = new Wallet(sourceWalletId, "John Doe", "john@example.com", BigDecimal.valueOf(50.00), now, now);
        var destWallet = new Wallet(destinationWalletId, "Jane Doe", "jane@example.com", BigDecimal.valueOf(50.00), now, now);
        var request = new TransferRequest(sourceWalletId, destinationWalletId, BigDecimal.valueOf(100.00), "Test transfer", "op-insufficient");

        when(transferOperationRepository.findByOperationId("op-insufficient")).thenReturn(Optional.empty());
        when(walletRepository.findById(sourceWalletId)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(destinationWalletId)).thenReturn(Optional.of(destWallet));

        assertThrows(InsufficientBalanceException.class, () -> transferUseCase.execute(request));
    }

    private static String payloadHashOf(TransferRequest request) {
        String payload = request.sourceWalletId() + "|" + request.destinationWalletId() + "|"
                + request.amount() + "|" + request.description();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
