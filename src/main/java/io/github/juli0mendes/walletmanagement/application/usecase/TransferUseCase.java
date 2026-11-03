package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.exception.DuplicateTransferOperationException;
import io.github.juli0mendes.walletmanagement.domain.exception.UnknownTransactionCommitResultException;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class TransferUseCase {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransferOperationRepository transferOperationRepository;

    public TransferUseCase(WalletRepository walletRepository,
                           TransactionRepository transactionRepository,
                           TransferOperationRepository transferOperationRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.transferOperationRepository = transferOperationRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public TransferResponse execute(TransferRequest request) {
        String operationId = request.operationId();
        String payloadHash = buildPayloadHash(request);
        Optional<TransferOperation> existingOperation = transferOperationRepository.findByOperationId(operationId);

        if (existingOperation.isPresent()) {
            TransferOperation operation = existingOperation.get();
            if (!operation.getPayloadHash().equals(payloadHash)) {
                throw new DuplicateTransferOperationException(
                        "Transfer operation id already exists with a different payload"
                );
            }
            return toResponse(request, operation.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        TransferOperation transferOperation = new TransferOperation(
                UUID.randomUUID(),
                operationId,
                payloadHash,
                TransferOperationStatus.INITIATED,
                request.description(),
                now,
                now
        );
        transferOperationRepository.save(transferOperation);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionSynchronization.STATUS_UNKNOWN) {
                        transferOperationRepository.updateStatus(operationId, TransferOperationStatus.UNKNOWN_COMMIT_RESULT);
                    }
                }
            });
        }

        UUID sourceWalletId = request.sourceWalletId();
        UUID destinationWalletId = request.destinationWalletId();
        BigDecimal amount = request.amount();

        Wallet sourceWallet = walletRepository.findById(sourceWalletId)
                .orElseThrow(() -> new WalletNotFoundException(sourceWalletId.toString()));

        Wallet destinationWallet = walletRepository.findById(destinationWalletId)
                .orElseThrow(() -> new WalletNotFoundException(destinationWalletId.toString()));

        sourceWallet.withdraw(amount);
        destinationWallet.deposit(amount);

        walletRepository.save(sourceWallet);
        walletRepository.save(destinationWallet);

        Transaction sourceTransaction = new Transaction(
                UUID.randomUUID(),
                sourceWalletId,
                TransactionType.TRANSFER_OUT,
                amount,
                sourceWallet.getBalance(),
                request.description(),
                now
        );
        transactionRepository.save(sourceTransaction);

        Transaction destTransaction = new Transaction(
                UUID.randomUUID(),
                destinationWalletId,
                TransactionType.TRANSFER_IN,
                amount,
                destinationWallet.getBalance(),
                request.description(),
                now
        );
        transactionRepository.save(destTransaction);

        transferOperationRepository.updateStatus(operationId, TransferOperationStatus.APPLIED);

        return new TransferResponse(
                UUID.randomUUID(),
                operationId,
                sourceWalletId,
                destinationWalletId,
                amount,
                "COMPLETED",
                request.description(),
                now
        );
    }

    private TransferResponse toResponse(TransferRequest request, TransferOperationStatus status) {
        String responseStatus = switch (status) {
            case UNKNOWN_COMMIT_RESULT -> "UNKNOWN_COMMIT_RESULT";
            case REJECTED -> "REJECTED";
            case RECONCILING -> "RECONCILING";
            default -> "COMPLETED";
        };

        if (status == TransferOperationStatus.UNKNOWN_COMMIT_RESULT) {
            throw new UnknownTransactionCommitResultException(
                    "Transfer result is still unknown; reconciliation required before retry"
            );
        }

        return new TransferResponse(
                UUID.randomUUID(),
                request.operationId(),
                request.sourceWalletId(),
                request.destinationWalletId(),
                request.amount(),
                responseStatus,
                request.description(),
                LocalDateTime.now()
        );
    }

    private String buildPayloadHash(TransferRequest request) {
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
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
