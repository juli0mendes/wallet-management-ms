package io.github.juli0mendes.walletmanagement.service;

import io.github.juli0mendes.walletmanagement.dto.TransferRequest;
import io.github.juli0mendes.walletmanagement.dto.TransferResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService Unit Tests")
class TransferServiceTest {

    private final TransferService transferService = new TransferService();

    @Test
    @DisplayName("Should successfully process transfer with valid request")
    void shouldProcessTransferSuccessfully() {
        // Given
        var request = new TransferRequest(
                java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID(),
                java.math.BigDecimal.valueOf(100.00),
                "Test transfer"
        );

        // When
        var response = transferService.transfer(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getTransferId());
        assertEquals(request.getSourceWalletId(), response.getSourceWalletId());
        assertEquals(request.getDestinationWalletId(), response.getDestinationWalletId());
        assertEquals(request.getAmount(), response.getAmount());
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(request.getDescription(), response.getDescription());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle transfer with null description")
    void shouldHandleTransferWithNullDescription() {
        // Given
        var request = new TransferRequest(
                java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID(),
                java.math.BigDecimal.valueOf(50.00),
                null
        );

        // When
        var response = transferService.transfer(request);

        // Then
        assertNotNull(response);
        assertNull(response.getDescription());
        assertEquals("COMPLETED", response.getStatus());
    }

    @Test
    @DisplayName("Should generate unique transfer IDs for different requests")
    void shouldGenerateUniqueTransferIds() {
        // Given
        var request1 = new TransferRequest(
                java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID(),
                java.math.BigDecimal.valueOf(25.00),
                "First transfer"
        );
        var request2 = new TransferRequest(
                java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID(),
                java.math.BigDecimal.valueOf(75.00),
                "Second transfer"
        );

        // When
        var response1 = transferService.transfer(request1);
        var response2 = transferService.transfer(request2);

        // Then
        assertNotEquals(response1.getTransferId(), response2.getTransferId());
        assertNotEquals(response1.getCreatedAt(), response2.getCreatedAt());
    }
}
