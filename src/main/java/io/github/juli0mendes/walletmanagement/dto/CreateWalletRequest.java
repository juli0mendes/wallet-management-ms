package io.github.juli0mendes.walletmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateWalletRequest(
        @NotBlank(message = "Owner name is required")
        @JsonProperty("owner_name")
        String ownerName,

        @NotBlank(message = "Owner email is required")
        @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "Invalid email format")
        @JsonProperty("owner_email")
        String ownerEmail
) {
}
