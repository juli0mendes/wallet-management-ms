package io.github.juli0mendes.walletmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull(message = "Source wallet ID is required")
        @JsonProperty("source_wallet_id")
        UUID sourceWalletId,

        @NotNull(message = "Destination wallet ID is required")
        @JsonProperty("destination_wallet_id")
        UUID destinationWalletId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @JsonProperty("description")
        String description
) {
}
