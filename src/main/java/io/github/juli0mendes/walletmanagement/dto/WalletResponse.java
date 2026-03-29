package io.github.juli0mendes.walletmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WalletResponse(
        @JsonProperty("wallet_id")
        UUID walletId,

        @JsonProperty("owner_name")
        String ownerName,

        @JsonProperty("owner_email")
        String ownerEmail,

        @JsonProperty("current_balance")
        BigDecimal currentBalance,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {
}
