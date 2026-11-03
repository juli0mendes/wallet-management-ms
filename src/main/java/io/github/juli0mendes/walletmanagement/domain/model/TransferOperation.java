package io.github.juli0mendes.walletmanagement.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class TransferOperation {
    private final UUID id;
    private final String operationId;
    private final String payloadHash;
    private final TransferOperationStatus status;
    private final String description;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public TransferOperation(UUID id, String operationId, String payloadHash,
                             TransferOperationStatus status, String description,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Transfer operation id cannot be null");
        this.operationId = Objects.requireNonNull(operationId, "Operation id cannot be null");
        this.payloadHash = Objects.requireNonNull(payloadHash, "Payload hash cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.description = description;
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at cannot be null");
    }

    public UUID getId() {
        return id;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getPayloadHash() {
        return payloadHash;
    }

    public TransferOperationStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
