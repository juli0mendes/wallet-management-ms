package io.github.juli0mendes.walletmanagement.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionResponse Unit Tests")
class TransactionResponseTest {

    @Test
    @DisplayName("Should expose TransactionResponse record accessors")
    void shouldTestTransactionResponse() {
        var transactionId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var type = "DEPOSIT";
        var amount = BigDecimal.valueOf(100.00);
        var balanceAfter = BigDecimal.valueOf(150.00);
        var description = "Test transaction";
        var createdAt = LocalDateTime.now();

        var response = new TransactionResponse(transactionId, walletId, type, amount, balanceAfter, description, createdAt);

        assertEquals(transactionId, response.transactionId());
        assertEquals(walletId, response.walletId());
        assertEquals(type, response.type());
        assertEquals(amount, response.amount());
        assertEquals(balanceAfter, response.balanceAfter());
        assertEquals(description, response.description());
        assertEquals(createdAt, response.createdAt());
    }
}
