package io.github.juli0mendes.walletmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BalanceResponse(
        @JsonProperty("current_balance")
        BigDecimal currentBalance,

        @JsonProperty("currency")
        String currency,

        @JsonProperty("retrieved_at")
        LocalDateTime retrievedAt
) {
}
