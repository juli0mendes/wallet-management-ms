package io.github.juli0mendes.walletmanagement.application.service;

import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.TransactionType;
import io.github.juli0mendes.walletmanagement.domain.model.TransferOperation;
import io.github.juli0mendes.walletmanagement.domain.model.TransferOperationStatus;
import io.github.juli0mendes.walletmanagement.domain.repository.TransactionRepository;
import io.github.juli0mendes.walletmanagement.domain.repository.TransferOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransferOperationReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(TransferOperationReconciliationService.class);
    private static final Duration LEASE_DURATION = Duration.ofMinutes(5);
    private static final Duration INITIATED_RECOVERY_STALE_AFTER = Duration.ofMinutes(5);

    private final TransferOperationRepository transferOperationRepository;
    private final TransactionRepository transactionRepository;

    public TransferOperationReconciliationService(TransferOperationRepository transferOperationRepository,
                                                 TransactionRepository transactionRepository) {
        this.transferOperationRepository = transferOperationRepository;
        this.transactionRepository = transactionRepository;
    }

    public void reconcileUnknownOperations(int batchLimit) {
        List<TransferOperation> operations = transferOperationRepository.findPendingReconciliation(batchLimit, LocalDateTime.now());
        for (TransferOperation operation : operations) {
            reconcileOperation(operation);
        }
    }

    public boolean reconcileOperation(TransferOperation operation) {
        if (operation == null) {
            return false;
        }

        String operationId = operation.getOperationId();
        String leaseOwner = UUID.randomUUID().toString();
        LocalDateTime leaseExpiresAt = LocalDateTime.now().plus(LEASE_DURATION);

        if (operation.getStatus() == TransferOperationStatus.INITIATED && !isRecoverableInitiatedOperation(operation)) {
            return false;
        }

        if (!transferOperationRepository.acquireReconciliationLease(operationId, leaseOwner, leaseExpiresAt)) {
            return false;
        }

        log.info("Reconciling transfer operation operationId={} transferId={} status={} sourceWalletId={} destinationWalletId={} leaseOwner={}",
                operationId, operation.getTransferId(), operation.getStatus(), operation.getSourceWalletId(), operation.getDestinationWalletId(), leaseOwner);

        boolean hasFinancialEvidence = hasConfirmedFinancialEvidence(operation);
        if (hasFinancialEvidence) {
            boolean finalized = transferOperationRepository.finalizeReconciliation(operationId, TransferOperationStatus.APPLIED, leaseOwner);
            if (finalized) {
                log.info("Transfer operation reconciled as applied operationId={} transferId={}", operationId, operation.getTransferId());
                return true;
            }
            log.warn("Transfer operation lease lost during reconciliation operationId={} transferId={}", operationId, operation.getTransferId());
            return false;
        }

        if (operation.getStatus() == TransferOperationStatus.INITIATED) {
            boolean finalized = transferOperationRepository.finalizeReconciliation(operationId, TransferOperationStatus.UNKNOWN_COMMIT_RESULT, leaseOwner);
            if (finalized) {
                log.warn("Stale INITIATED operation moved to UNKNOWN_COMMIT_RESULT operationId={} transferId={}", operationId, operation.getTransferId());
                return false;
            }
            log.warn("Unable to finalize stale INITIATED operation operationId={} transferId={}", operationId, operation.getTransferId());
            return false;
        }

        boolean finalized = transferOperationRepository.finalizeReconciliation(operationId, TransferOperationStatus.UNKNOWN_COMMIT_RESULT, leaseOwner);
        if (finalized) {
            log.warn("Transfer operation remains indeterminate after reconciliation operationId={} transferId={}", operationId, operation.getTransferId());
            return false;
        }

        log.warn("Unable to finalize indeterminate operation operationId={} transferId={}", operationId, operation.getTransferId());
        return false;
    }

    private boolean isRecoverableInitiatedOperation(TransferOperation operation) {
        if (operation == null || operation.getStatus() != TransferOperationStatus.INITIATED) {
            return false;
        }

        if (operation.getUpdatedAt() == null) {
            return false;
        }

        Duration elapsed = Duration.between(operation.getUpdatedAt(), LocalDateTime.now());
        return !elapsed.isNegative() && elapsed.compareTo(INITIATED_RECOVERY_STALE_AFTER) >= 0 && !hasConfirmedFinancialEvidence(operation);
    }

    private boolean hasConfirmedFinancialEvidence(TransferOperation operation) {
        if (operation.getTransferId() == null) {
            return false;
        }

        List<Transaction> transferTransactions = transactionRepository.findByTransferId(operation.getTransferId());
        if (transferTransactions.size() != 2) {
            return false;
        }

        boolean sourceEvidence = transferTransactions.stream().anyMatch(transaction ->
                transaction.getType() == TransactionType.TRANSFER_OUT
                        && transaction.getTransferId() != null
                        && transaction.getTransferId().equals(operation.getTransferId())
                        && transaction.getWalletId().equals(operation.getSourceWalletId())
                        && transaction.getAmount().compareTo(operation.getAmount()) == 0
                        && transaction.getOperationId() != null
                        && transaction.getOperationId().equals(operation.getOperationId()));

        boolean destinationEvidence = transferTransactions.stream().anyMatch(transaction ->
                transaction.getType() == TransactionType.TRANSFER_IN
                        && transaction.getTransferId() != null
                        && transaction.getTransferId().equals(operation.getTransferId())
                        && transaction.getWalletId().equals(operation.getDestinationWalletId())
                        && transaction.getAmount().compareTo(operation.getAmount()) == 0
                        && transaction.getOperationId() != null
                        && transaction.getOperationId().equals(operation.getOperationId()));

        return sourceEvidence && destinationEvidence;
    }
}
