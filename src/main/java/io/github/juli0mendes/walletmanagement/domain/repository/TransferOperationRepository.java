package io.github.juli0mendes.walletmanagement.domain.repository;

import io.github.juli0mendes.walletmanagement.domain.model.TransferOperation;
import io.github.juli0mendes.walletmanagement.domain.model.TransferOperationStatus;

import java.util.Optional;

public interface TransferOperationRepository {
    Optional<TransferOperation> findByOperationId(String operationId);
    TransferOperation save(TransferOperation transferOperation);
    void updateStatus(String operationId, TransferOperationStatus status);
}
