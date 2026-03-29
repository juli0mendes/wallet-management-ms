package io.github.juli0mendes.walletmanagement.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransferResponse Unit Tests")
class TransferResponseTest {

    @Test
    @DisplayName("Should test TransferResponse getters and setters")
    void shouldTestTransferResponse() {
        var transferId = UUID.randomUUID();
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var amount = BigDecimal.valueOf(100.00);
        var status = "COMPLETED";
        var description = "Test transfer response";
        var createdAt = LocalDateTime.now();

        var response = new TransferResponse(transferId, sourceWalletId, destinationWalletId, amount, status, description, createdAt);

        assertEquals(transferId, response.getTransferId());
        assertEquals(sourceWalletId, response.getSourceWalletId());
        assertEquals(destinationWalletId, response.getDestinationWalletId());
        assertEquals(amount, response.getAmount());
        assertEquals(status, response.getStatus());
        assertEquals(description, response.getDescription());
        assertEquals(createdAt, response.getCreatedAt());

        var newTransferId = UUID.randomUUID();
        var newSourceWalletId = UUID.randomUUID();
        var newDestinationWalletId = UUID.randomUUID();
        var newAmount = BigDecimal.valueOf(200.00);
        var newStatus = "PENDING";
        var newDescription = "Updated transfer response";
        var newCreatedAt = LocalDateTime.now().plusHours(2);

        response.setTransferId(newTransferId);
        response.setSourceWalletId(newSourceWalletId);
        response.setDestinationWalletId(newDestinationWalletId);
        response.setAmount(newAmount);
        response.setStatus(newStatus);
        response.setDescription(newDescription);
        response.setCreatedAt(newCreatedAt);

        assertEquals(newTransferId, response.getTransferId());
        assertEquals(newSourceWalletId, response.getSourceWalletId());
        assertEquals(newDestinationWalletId, response.getDestinationWalletId());
        assertEquals(newAmount, response.getAmount());
        assertEquals(newStatus, response.getStatus());
        assertEquals(newDescription, response.getDescription());
        assertEquals(newCreatedAt, response.getCreatedAt());
    }
}
