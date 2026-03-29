package io.github.juli0mendes.walletmanagement.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreateWalletRequest Unit Tests")
class CreateWalletRequestTest {

    @Test
    @DisplayName("Should expose CreateWalletRequest record accessors")
    void shouldTestCreateWalletRequest() {
        var ownerName = "John Doe";
        var ownerEmail = "john@example.com";

        var request = new CreateWalletRequest(ownerName, ownerEmail);

        assertEquals(ownerName, request.ownerName());
        assertEquals(ownerEmail, request.ownerEmail());
    }
}
