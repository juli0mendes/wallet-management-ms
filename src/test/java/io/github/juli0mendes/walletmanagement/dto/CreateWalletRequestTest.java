package io.github.juli0mendes.walletmanagement.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreateWalletRequest Unit Tests")
class CreateWalletRequestTest {

    @Test
    @DisplayName("Should test CreateWalletRequest getters and setters")
    void shouldTestCreateWalletRequest() {
        var ownerName = "John Doe";
        var ownerEmail = "john@example.com";

        var request = new CreateWalletRequest(ownerName, ownerEmail);

        assertEquals(ownerName, request.getOwnerName());
        assertEquals(ownerEmail, request.getOwnerEmail());

        var newName = "Jane Doe";
        var newEmail = "jane@example.com";

        request.setOwnerName(newName);
        request.setOwnerEmail(newEmail);

        assertEquals(newName, request.getOwnerName());
        assertEquals(newEmail, request.getOwnerEmail());
    }
}
