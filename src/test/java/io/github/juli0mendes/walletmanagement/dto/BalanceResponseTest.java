package io.github.juli0mendes.walletmanagement.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BalanceResponse Unit Tests")
class BalanceResponseTest {

    @Test
    @DisplayName("Should expose BalanceResponse record accessors")
    void shouldTestBalanceResponse() {
        var balance = BigDecimal.valueOf(100.50);
        var currency = "USD";
        var retrievedAt = LocalDateTime.now();

        var response = new BalanceResponse(balance, currency, retrievedAt);

        assertEquals(balance, response.currentBalance());
        assertEquals(currency, response.currency());
        assertEquals(retrievedAt, response.retrievedAt());
    }
}
