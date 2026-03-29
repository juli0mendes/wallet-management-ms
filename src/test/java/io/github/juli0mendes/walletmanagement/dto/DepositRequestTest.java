package io.github.juli0mendes.walletmanagement.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DepositRequest Unit Tests")
class DepositRequestTest {

    @Test
    @DisplayName("Should expose DepositRequest record accessors")
    void shouldTestDepositRequest() {
        var amount = BigDecimal.valueOf(50.00);
        var description = "Test deposit";

        var request = new DepositRequest(amount, description);

        assertEquals(amount, request.amount());
        assertEquals(description, request.description());
    }
}
