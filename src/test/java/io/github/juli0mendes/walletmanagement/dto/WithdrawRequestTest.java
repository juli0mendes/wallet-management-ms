package io.github.juli0mendes.walletmanagement.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WithdrawRequest Unit Tests")
class WithdrawRequestTest {

    @Test
    @DisplayName("Should test WithdrawRequest getters and setters")
    void shouldTestWithdrawRequest() {
        var amount = BigDecimal.valueOf(30.00);
        var description = "Test withdrawal";

        var request = new WithdrawRequest(amount, description);

        assertEquals(amount, request.getAmount());
        assertEquals(description, request.getDescription());

        var newAmount = BigDecimal.valueOf(45.00);
        var newDescription = "Updated withdrawal";

        request.setAmount(newAmount);
        request.setDescription(newDescription);

        assertEquals(newAmount, request.getAmount());
        assertEquals(newDescription, request.getDescription());
    }
}
