package io.github.juli0mendes.walletmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.juli0mendes.walletmanagement.domain.model.TransferOperation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransferOperationResponse(
        @JsonProperty("operation_id") String operationId,
        @JsonProperty("transfer_id") UUID transferId,
        @JsonProperty("source_wallet_id") UUID sourceWalletId,
        @JsonProperty("destination_wallet_id") UUID destinationWalletId,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("status") String status,
        @JsonProperty("description") String description,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt
) {
    public static TransferOperationResponse fromDomain(TransferOperation operation) {
        return new TransferOperationResponse(
                operation.getOperationId(),
                operation.getTransferId(),
                operation.getSourceWalletId(),
                operation.getDestinationWalletId(),
                operation.getAmount(),
                operation.getStatus().name(),
                operation.getDescription(),
                operation.getCreatedAt(),
                operation.getUpdatedAt()
        );
    }
}
