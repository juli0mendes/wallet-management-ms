package io.github.juli0mendes.walletmanagement.application.service;

import io.github.juli0mendes.walletmanagement.domain.model.TransactionType;
import io.github.juli0mendes.walletmanagement.domain.model.TransferOperation;
import io.github.juli0mendes.walletmanagement.domain.model.TransferOperationStatus;
import io.github.juli0mendes.walletmanagement.domain.repository.TransactionRepository;
import io.github.juli0mendes.walletmanagement.domain.repository.TransferOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransferOperationReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(TransferOperationReconciliationService.class);

    private final TransferOperationRepository transferOperationRepository;
    private final TransactionRepository transactionRepository;

    public TransferOperationReconciliationService(TransferOperationRepository transferOperationRepository,
                                                 TransactionRepository transactionRepository) {
        this.transferOperationRepository = transferOperationRepository;
        this.transactionRepository = transactionRepository;
    }

    public void reconcileUnknownOperations(int batchLimit) {
        List<TransferOperation> operations = transferOperationRepository.findByStatus(TransferOperationStatus.UNKNOWN_COMMIT_RESULT, batchLimit);
        for (TransferOperation operation : operations) {
            reconcileOperation(operation);
        }
    }

    public boolean reconcileOperation(TransferOperation operation) {
        if (operation == null) {
            return false;
        }

        String operationId = operation.getOperationId();
        if (!transferOperationRepository.markReconciliationInProgress(operationId)) {
            return false;
        }

        log.info("Reconciling transfer operation operationId={} transferId={} status={} sourceWalletId={} destinationWalletId={}",
                operationId, operation.getTransferId(), operation.getStatus(), operation.getSourceWalletId(), operation.getDestinationWalletId());

        boolean hasFinancialEvidence = hasConfirmedFinancialEvidence(operation);
        if (hasFinancialEvidence) {
            transferOperationRepository.updateStatus(operationId, TransferOperationStatus.APPLIED);
            log.info("Transfer operation reconciled as applied operationId={} transferId={}", operationId, operation.getTransferId());
            return true;
        }

        transferOperationRepository.updateStatus(operationId, TransferOperationStatus.UNKNOWN_COMMIT_RESULT);
        log.warn("Transfer operation remains indeterminate after reconciliation operationId={} transferId={}", operationId, operation.getTransferId());
        return false;
    }

    private boolean hasConfirmedFinancialEvidence(TransferOperation operation) {
        var sourceTransactions = transactionRepository.findByWalletId(operation.getSourceWalletId());
        var destinationTransactions = transactionRepository.findByWalletId(operation.getDestinationWalletId());

        boolean sourceEvidence = sourceTransactions.stream().anyMatch(transaction ->
                transaction.getType() == TransactionType.TRANSFER_OUT
                        && transaction.getWalletId().equals(operation.getSourceWalletId())
                        && transaction.getAmount().compareTo(operation.getAmount()) == 0
                        && operation.getDescription() != null
                        && operation.getDescription().equals(transaction.getDescription()));

        boolean destinationEvidence = destinationTransactions.stream().anyMatch(transaction ->
                transaction.getType() == TransactionType.TRANSFER_IN
                        && transaction.getWalletId().equals(operation.getDestinationWalletId())
                        && transaction.getAmount().compareTo(operation.getAmount()) == 0
                        && operation.getDescription() != null
                        && operation.getDescription().equals(transaction.getDescription()));

        return sourceEvidence && destinationEvidence;
    }
}
