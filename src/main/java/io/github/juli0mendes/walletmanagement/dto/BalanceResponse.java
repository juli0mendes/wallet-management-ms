package io.github.juli0mendes.walletmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BalanceResponse {

    @JsonProperty("current_balance")
    private BigDecimal currentBalance;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("retrieved_at")
    private LocalDateTime retrievedAt;

    public BalanceResponse() {
    }

    public BalanceResponse(BigDecimal currentBalance, String currency, LocalDateTime retrievedAt) {
        this.currentBalance = currentBalance;
        this.currency = currency;
        this.retrievedAt = retrievedAt;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getRetrievedAt() {
        return retrievedAt;
    }

    public void setRetrievedAt(LocalDateTime retrievedAt) {
        this.retrievedAt = retrievedAt;
    }
}

