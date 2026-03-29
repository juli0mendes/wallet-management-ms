package io.github.juli0mendes.walletmanagement.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DepositRequest Unit Tests")
class DepositRequestTest {

    @Test
    @DisplayName("Should test DepositRequest getters and setters")
    void shouldTestDepositRequest() {
        var amount = BigDecimal.valueOf(50.00);
        var description = "Test deposit";

        var request = new DepositRequest(amount, description);

        assertEquals(amount, request.getAmount());
        assertEquals(description, request.getDescription());

        var newAmount = BigDecimal.valueOf(75.00);
        var newDescription = "Updated deposit";

        request.setAmount(newAmount);
        request.setDescription(newDescription);

        assertEquals(newAmount, request.getAmount());
        assertEquals(newDescription, request.getDescription());
    }
}
