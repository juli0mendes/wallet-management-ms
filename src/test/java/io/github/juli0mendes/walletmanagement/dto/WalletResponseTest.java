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
    @DisplayName("Should expose WalletResponse record accessors")
    void shouldTestWalletResponse() {
        var walletId = UUID.randomUUID();
        var ownerName = "Test User";
        var ownerEmail = "test@example.com";
        var currentBalance = BigDecimal.valueOf(250.00);
        var createdAt = LocalDateTime.now().minusDays(1);
        var updatedAt = LocalDateTime.now();

        var response = new WalletResponse(walletId, ownerName, ownerEmail, currentBalance, createdAt, updatedAt);

        assertEquals(walletId, response.walletId());
        assertEquals(ownerName, response.ownerName());
        assertEquals(ownerEmail, response.ownerEmail());
        assertEquals(currentBalance, response.currentBalance());
        assertEquals(createdAt, response.createdAt());
        assertEquals(updatedAt, response.updatedAt());
    }
}
