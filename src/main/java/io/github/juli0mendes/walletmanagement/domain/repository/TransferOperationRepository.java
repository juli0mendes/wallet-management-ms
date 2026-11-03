package io.github.juli0mendes.walletmanagement.domain.repository;

import io.github.juli0mendes.walletmanagement.domain.model.TransferOperation;
import io.github.juli0mendes.walletmanagement.domain.model.TransferOperationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransferOperationRepository {
    Optional<TransferOperation> findByOperationId(String operationId);
    List<TransferOperation> findByStatus(TransferOperationStatus status, int limit);
    List<TransferOperation> findPendingReconciliation(int limit, LocalDateTime asOf);
    TransferOperation save(TransferOperation transferOperation);
    boolean updateStatus(String operationId, TransferOperationStatus status);
    boolean updateStatus(String operationId, TransferOperationStatus status, String expectedLeaseOwner);
    boolean markExecutionInProgress(String operationId, String executionOwner, LocalDateTime leaseExpiresAt);
    boolean markReconciliationInProgress(String operationId);
    boolean acquireReconciliationLease(String operationId, String leaseOwner, LocalDateTime leaseExpiresAt);
    boolean finalizeReconciliation(String operationId, TransferOperationStatus status, String leaseOwner);
}
