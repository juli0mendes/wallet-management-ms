package io.github.juli0mendes.walletmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.juli0mendes.walletmanagement.application.usecase.TransferUseCase;
import io.github.juli0mendes.walletmanagement.dto.TransferRequest;
import io.github.juli0mendes.walletmanagement.dto.TransferResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
@DisplayName("TransferController Integration Tests")
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransferUseCase transferUseCase;

    @Test
    @DisplayName("Should process transfer successfully")
    void shouldProcessTransferSuccessfully() throws Exception {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var request = new TransferRequest(sourceWalletId, destinationWalletId,
                BigDecimal.valueOf(100.00), "Test transfer");

        var response = new TransferResponse(UUID.randomUUID(), sourceWalletId, destinationWalletId,
                BigDecimal.valueOf(100.00), "COMPLETED", "Test transfer", LocalDateTime.now());

        when(transferUseCase.execute(any(TransferRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transfer_id").isNotEmpty())
                .andExpect(jsonPath("$.source_wallet_id").value(sourceWalletId.toString()))
                .andExpect(jsonPath("$.destination_wallet_id").value(destinationWalletId.toString()))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Should return bad request for invalid transfer request - null source wallet")
    void shouldReturnBadRequestForInvalidTransferRequestNullSource() throws Exception {
        var request = new TransferRequest(null, UUID.randomUUID(),
                BigDecimal.valueOf(100.00), "Test transfer");

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.sourceWalletId").value("Source wallet ID is required"));
    }

    @Test
    @DisplayName("Should return bad request for invalid transfer request - negative amount")
    void shouldReturnBadRequestForInvalidTransferRequestNegativeAmount() throws Exception {
        var request = new TransferRequest(UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.valueOf(-10.00), "Test transfer");

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.amount").value("Amount must be greater than 0"));
    }

    @Test
    @DisplayName("Should return bad request for invalid transfer request - zero amount")
    void shouldReturnBadRequestForInvalidTransferRequestZeroAmount() throws Exception {
        var request = new TransferRequest(UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.ZERO, "Test transfer");

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.amount").value("Amount must be greater than 0"));
    }
}
