package io.github.juli0mendes.walletmanagement.infrastructure.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "transfer_operations")
public class TransferOperationDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String operationId;

    private String transferId;
    private String sourceWalletId;
    private String destinationWalletId;
    private BigDecimal amount;
    private String payloadHash;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TransferOperationDocument() {
    }

    public TransferOperationDocument(String id, String operationId, String payloadHash,
                                    String status, String description,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, operationId, null, null, null, null, payloadHash, status, description, createdAt, updatedAt);
    }

    public TransferOperationDocument(String id, String operationId, String transferId,
                                    String sourceWalletId, String destinationWalletId,
                                    BigDecimal amount, String payloadHash,
                                    String status, String description,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.operationId = operationId;
        this.transferId = transferId;
        this.sourceWalletId = sourceWalletId;
        this.destinationWalletId = destinationWalletId;
        this.amount = amount;
        this.payloadHash = payloadHash;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }

    public String getTransferId() { return transferId; }
    public void setTransferId(String transferId) { this.transferId = transferId; }

    public String getSourceWalletId() { return sourceWalletId; }
    public void setSourceWalletId(String sourceWalletId) { this.sourceWalletId = sourceWalletId; }

    public String getDestinationWalletId() { return destinationWalletId; }
    public void setDestinationWalletId(String destinationWalletId) { this.destinationWalletId = destinationWalletId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPayloadHash() { return payloadHash; }
    public void setPayloadHash(String payloadHash) { this.payloadHash = payloadHash; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
