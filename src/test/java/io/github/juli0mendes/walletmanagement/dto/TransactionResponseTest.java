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
    @DisplayName("Should test TransactionResponse getters and setters")
    void shouldTestTransactionResponse() {
        var transactionId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var type = "DEPOSIT";
        var amount = BigDecimal.valueOf(100.00);
        var balanceAfter = BigDecimal.valueOf(150.00);
        var description = "Test transaction";
        var createdAt = LocalDateTime.now();

        var response = new TransactionResponse(transactionId, walletId, type, amount, balanceAfter, description, createdAt);

        assertEquals(transactionId, response.getTransactionId());
        assertEquals(walletId, response.getWalletId());
        assertEquals(type, response.getType());
        assertEquals(amount, response.getAmount());
        assertEquals(balanceAfter, response.getBalanceAfter());
        assertEquals(description, response.getDescription());
        assertEquals(createdAt, response.getCreatedAt());

        var newTransactionId = UUID.randomUUID();
        var newWalletId = UUID.randomUUID();
        var newType = "WITHDRAW";
        var newAmount = BigDecimal.valueOf(25.00);
        var newBalanceAfter = BigDecimal.valueOf(125.00);
        var newDescription = "Updated transaction";
        var newCreatedAt = LocalDateTime.now().plusMinutes(5);

        response.setTransactionId(newTransactionId);
        response.setWalletId(newWalletId);
        response.setType(newType);
        response.setAmount(newAmount);
        response.setBalanceAfter(newBalanceAfter);
        response.setDescription(newDescription);
        response.setCreatedAt(newCreatedAt);

        assertEquals(newTransactionId, response.getTransactionId());
        assertEquals(newWalletId, response.getWalletId());
        assertEquals(newType, response.getType());
        assertEquals(newAmount, response.getAmount());
        assertEquals(newBalanceAfter, response.getBalanceAfter());
        assertEquals(newDescription, response.getDescription());
        assertEquals(newCreatedAt, response.getCreatedAt());
    }
}
