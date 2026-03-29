package io.github.juli0mendes.walletmanagement.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransferRequest Unit Tests")
class TransferRequestTest {

    @Test
    @DisplayName("Should expose TransferRequest record accessors")
    void shouldTestTransferRequest() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var amount = BigDecimal.valueOf(50.00);
        var description = "Test transfer";

        var request = new TransferRequest(sourceWalletId, destinationWalletId, amount, description);

        assertEquals(sourceWalletId, request.sourceWalletId());
        assertEquals(destinationWalletId, request.destinationWalletId());
        assertEquals(amount, request.amount());
        assertEquals(description, request.description());
    }
}
