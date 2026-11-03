package io.github.juli0mendes.walletmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransferResponse {

    @JsonProperty("transfer_id")
    private UUID transferId;

    @JsonProperty("operation_id")
    private String operationId;

    @JsonProperty("source_wallet_id")
    private UUID sourceWalletId;

    @JsonProperty("destination_wallet_id")
    private UUID destinationWalletId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("status")
    private String status;

    @JsonProperty("description")
    private String description;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public TransferResponse() {
    }

    public TransferResponse(UUID transferId, String operationId, UUID sourceWalletId, UUID destinationWalletId,
                           BigDecimal amount, String status, String description, LocalDateTime createdAt) {
        this.transferId = transferId;
        this.operationId = operationId;
        this.sourceWalletId = sourceWalletId;
        this.destinationWalletId = destinationWalletId;
        this.amount = amount;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
    }

    public TransferResponse(UUID transferId, UUID sourceWalletId, UUID destinationWalletId,
                           BigDecimal amount, String status, String description, LocalDateTime createdAt) {
        this(transferId, null, sourceWalletId, destinationWalletId, amount, status, description, createdAt);
    }

    public UUID getTransferId() {
        return transferId;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public void setTransferId(UUID transferId) {
        this.transferId = transferId;
    }

    public UUID getSourceWalletId() {
        return sourceWalletId;
    }

    public void setSourceWalletId(UUID sourceWalletId) {
        this.sourceWalletId = sourceWalletId;
    }

    public UUID getDestinationWalletId() {
        return destinationWalletId;
    }

    public void setDestinationWalletId(UUID destinationWalletId) {
        this.destinationWalletId = destinationWalletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

