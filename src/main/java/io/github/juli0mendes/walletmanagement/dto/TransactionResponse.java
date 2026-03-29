package io.github.juli0mendes.walletmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        @JsonProperty("transaction_id")
        UUID transactionId,

        @JsonProperty("wallet_id")
        UUID walletId,

        @JsonProperty("type")
        String type,

        @JsonProperty("amount")
        BigDecimal amount,

        @JsonProperty("balance_after")
        BigDecimal balanceAfter,

        @JsonProperty("description")
        String description,

        @JsonProperty("created_at")
        LocalDateTime createdAt
) {
}
