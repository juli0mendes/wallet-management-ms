package io.github.juli0mendes.walletmanagement.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WalletResponse Unit Tests")
class WalletResponseTest {

    @Test
    @DisplayName("Should test WalletResponse getters and setters")
    void shouldTestWalletResponse() {
        var walletId = UUID.randomUUID();
        var ownerName = "Test User";
        var ownerEmail = "test@example.com";
        var currentBalance = BigDecimal.valueOf(250.00);
        var createdAt = LocalDateTime.now().minusDays(1);
        var updatedAt = LocalDateTime.now();

        var response = new WalletResponse(walletId, ownerName, ownerEmail, currentBalance, createdAt, updatedAt);

        assertEquals(walletId, response.getWalletId());
        assertEquals(ownerName, response.getOwnerName());
        assertEquals(ownerEmail, response.getOwnerEmail());
        assertEquals(currentBalance, response.getCurrentBalance());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());

        var newWalletId = UUID.randomUUID();
        var newOwnerName = "Updated User";
        var newOwnerEmail = "updated@example.com";
        var newCurrentBalance = BigDecimal.valueOf(300.00);
        var newCreatedAt = LocalDateTime.now().minusDays(2);
        var newUpdatedAt = LocalDateTime.now().plusMinutes(10);

        response.setWalletId(newWalletId);
        response.setOwnerName(newOwnerName);
        response.setOwnerEmail(newOwnerEmail);
        response.setCurrentBalance(newCurrentBalance);
        response.setCreatedAt(newCreatedAt);
        response.setUpdatedAt(newUpdatedAt);

        assertEquals(newWalletId, response.getWalletId());
        assertEquals(newOwnerName, response.getOwnerName());
        assertEquals(newOwnerEmail, response.getOwnerEmail());
        assertEquals(newCurrentBalance, response.getCurrentBalance());
        assertEquals(newCreatedAt, response.getCreatedAt());
        assertEquals(newUpdatedAt, response.getUpdatedAt());
    }
}
