package io.github.juli0mendes.walletmanagement.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class TransferOperation {
    private final UUID id;
    private final String operationId;
    private final UUID transferId;
    private final UUID sourceWalletId;
    private final UUID destinationWalletId;
    private final BigDecimal amount;
    private final String payloadHash;
    private final TransferOperationStatus status;
    private final String description;
    private final String leaseOwner;
    private final LocalDateTime leaseExpiresAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public TransferOperation(UUID id, String operationId, String payloadHash,
                             TransferOperationStatus status, String description,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, operationId, UUID.randomUUID(), null, null, null, payloadHash, status, description, null, null, createdAt, updatedAt);
    }

    public TransferOperation(UUID id, String operationId, UUID transferId,
                            UUID sourceWalletId, UUID destinationWalletId,
                            BigDecimal amount, String payloadHash,
                            TransferOperationStatus status, String description,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, operationId, transferId, sourceWalletId, destinationWalletId, amount, payloadHash,
                status, description, null, null, createdAt, updatedAt);
    }

    public TransferOperation(UUID id, String operationId, UUID transferId,
                            UUID sourceWalletId, UUID destinationWalletId,
                            BigDecimal amount, String payloadHash,
                            TransferOperationStatus status, String description,
                            String leaseOwner, LocalDateTime leaseExpiresAt,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Transfer operation id cannot be null");
        this.operationId = Objects.requireNonNull(operationId, "Operation id cannot be null");
        this.transferId = Objects.requireNonNull(transferId, "Transfer id cannot be null");
        this.sourceWalletId = sourceWalletId;
        this.destinationWalletId = destinationWalletId;
        this.amount = amount;
        this.payloadHash = Objects.requireNonNull(payloadHash, "Payload hash cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.description = description;
        this.leaseOwner = leaseOwner;
        this.leaseExpiresAt = leaseExpiresAt;
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at cannot be null");
    }

    public UUID getId() {
        return id;
    }

    public String getOperationId() {
        return operationId;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public UUID getSourceWalletId() {
        return sourceWalletId;
    }

    public UUID getDestinationWalletId() {
        return destinationWalletId;
    }

    public BigDecimal getAmount() {
        return amount;
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

    public String getLeaseOwner() {
        return leaseOwner;
    }

    public LocalDateTime getLeaseExpiresAt() {
        return leaseExpiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
