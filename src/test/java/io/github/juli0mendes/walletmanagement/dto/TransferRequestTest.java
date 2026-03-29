package io.github.juli0mendes.walletmanagement.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransferRequest Unit Tests")
class TransferRequestTest {

    @Test
    @DisplayName("Should test TransferRequest getters and setters")
    void shouldTestTransferRequest() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var amount = BigDecimal.valueOf(50.00);
        var description = "Test transfer";

        var request = new TransferRequest(sourceWalletId, destinationWalletId, amount, description);

        assertEquals(sourceWalletId, request.getSourceWalletId());
        assertEquals(destinationWalletId, request.getDestinationWalletId());
        assertEquals(amount, request.getAmount());
        assertEquals(description, request.getDescription());

        var newSourceWalletId = UUID.randomUUID();
        var newDestinationWalletId = UUID.randomUUID();
        var newAmount = BigDecimal.valueOf(75.00);
        var newDescription = "Updated transfer";

        request.setSourceWalletId(newSourceWalletId);
        request.setDestinationWalletId(newDestinationWalletId);
        request.setAmount(newAmount);
        request.setDescription(newDescription);

        assertEquals(newSourceWalletId, request.getSourceWalletId());
        assertEquals(newDestinationWalletId, request.getDestinationWalletId());
        assertEquals(newAmount, request.getAmount());
        assertEquals(newDescription, request.getDescription());
    }
}
