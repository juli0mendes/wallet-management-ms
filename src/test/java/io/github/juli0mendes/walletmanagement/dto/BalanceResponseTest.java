package io.github.juli0mendes.walletmanagement.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BalanceResponse Unit Tests")
class BalanceResponseTest {

    @Test
    @DisplayName("Should test BalanceResponse getters and setters")
    void shouldTestBalanceResponse() {
        var balance = BigDecimal.valueOf(100.50);
        var currency = "USD";
        var retrievedAt = LocalDateTime.now();

        var response = new BalanceResponse(balance, currency, retrievedAt);

        assertEquals(balance, response.getCurrentBalance());
        assertEquals(currency, response.getCurrency());
        assertEquals(retrievedAt, response.getRetrievedAt());

        var newBalance = BigDecimal.valueOf(200.00);
        var newCurrency = "EUR";
        var newRetrievedAt = LocalDateTime.now().plusHours(1);

        response.setCurrentBalance(newBalance);
        response.setCurrency(newCurrency);
        response.setRetrievedAt(newRetrievedAt);

        assertEquals(newBalance, response.getCurrentBalance());
        assertEquals(newCurrency, response.getCurrency());
        assertEquals(newRetrievedAt, response.getRetrievedAt());
    }
}
