package io.github.juli0mendes.walletmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.juli0mendes.walletmanagement.dto.*;
import io.github.juli0mendes.walletmanagement.exception.WalletNotFoundException;
import io.github.juli0mendes.walletmanagement.service.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("deprecation")
@WebMvcTest(WalletController.class)
@DisplayName("WalletController Integration Tests")
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WalletService walletService;

    @Test
    @DisplayName("Should create wallet successfully")
    void shouldCreateWalletSuccessfully() throws Exception {
        var request = new CreateWalletRequest("John Doe", "john@example.com");
        var response = new WalletResponse(
                UUID.randomUUID(), "John Doe", "john@example.com",
                BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now()
        );
        when(walletService.createWallet(any(CreateWalletRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.owner_name").value("John Doe"))
                .andExpect(jsonPath("$.owner_email").value("john@example.com"));
    }

    @Test
    @DisplayName("Should return bad request for invalid create wallet request - blank name")
    void shouldReturnBadRequestForInvalidCreateWalletBlankName() throws Exception {
        var request = new CreateWalletRequest("", "john@example.com");

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.ownerName").value("Owner name is required"));
    }

    @Test
    @DisplayName("Should return bad request for invalid create wallet request - invalid email")
    void shouldReturnBadRequestForInvalidCreateWalletInvalidEmail() throws Exception {
        var request = new CreateWalletRequest("John Doe", "invalid-email");

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.ownerEmail").value("Invalid email format"));
    }

    @Test
    @DisplayName("Should get wallet successfully")
    void shouldGetWalletSuccessfully() throws Exception {
        var walletId = UUID.randomUUID();
        var response = new WalletResponse(
                walletId, "John Doe", "john@example.com",
                BigDecimal.valueOf(100.00), LocalDateTime.now(), LocalDateTime.now()
        );
        when(walletService.getWallet(walletId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/wallets/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wallet_id").value(walletId.toString()))
                .andExpect(jsonPath("$.owner_name").value("John Doe"));
    }

    @Test
    @DisplayName("Should return 404 when wallet not found")
    void shouldReturn404WhenWalletNotFound() throws Exception {
        var walletId = UUID.randomUUID();
        when(walletService.getWallet(walletId)).thenThrow(new WalletNotFoundException(walletId.toString()));

        mockMvc.perform(get("/api/v1/wallets/{walletId}", walletId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code").value("WALLET_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Wallet not found with id: " + walletId));
    }

    @Test
    @DisplayName("Should get balance successfully")
    void shouldGetBalanceSuccessfully() throws Exception {
        var walletId = UUID.randomUUID();
        var response = new BalanceResponse(BigDecimal.valueOf(150.00), "USD", LocalDateTime.now());
        when(walletService.getBalance(eq(walletId), isNull())).thenReturn(response);

        mockMvc.perform(get("/api/v1/wallets/{walletId}/balance", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.current_balance").value(150.00))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    @DisplayName("Should return 404 when getting balance for non-existent wallet")
    void shouldReturn404WhenGettingBalanceForNonExistentWallet() throws Exception {
        var walletId = UUID.randomUUID();
        when(walletService.getBalance(eq(walletId), isNull())).thenThrow(new WalletNotFoundException(walletId.toString()));

        mockMvc.perform(get("/api/v1/wallets/{walletId}/balance", walletId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code").value("WALLET_NOT_FOUND"));
    }

    @Test
    @DisplayName("Should deposit successfully")
    void shouldDepositSuccessfully() throws Exception {
        var walletId = UUID.randomUUID();
        var request = new DepositRequest(BigDecimal.valueOf(50.00), "Test deposit");
        var response = new TransactionResponse(
                UUID.randomUUID(), walletId, "DEPOSIT", BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(50.00), "Test deposit", LocalDateTime.now()
        );
        when(walletService.deposit(eq(walletId), any(DepositRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/wallets/{walletId}/deposit", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(50.00));
    }

    @Test
    @DisplayName("Should return bad request for invalid deposit - null amount")
    void shouldReturnBadRequestForInvalidDepositNullAmount() throws Exception {
        var walletId = UUID.randomUUID();
        var request = new DepositRequest(null, "Test deposit");

        mockMvc.perform(post("/api/v1/wallets/{walletId}/deposit", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.amount").value("Amount is required"));
    }

    @Test
    @DisplayName("Should return bad request for invalid deposit - zero amount")
    void shouldReturnBadRequestForInvalidDepositZeroAmount() throws Exception {
        var walletId = UUID.randomUUID();
        var request = new DepositRequest(BigDecimal.ZERO, "Test deposit");

        mockMvc.perform(post("/api/v1/wallets/{walletId}/deposit", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.amount").value("Amount must be greater than 0"));
    }

    @Test
    @DisplayName("Should return 404 when depositing to non-existent wallet")
    void shouldReturn404WhenDepositingToNonExistentWallet() throws Exception {
        var walletId = UUID.randomUUID();
        var request = new DepositRequest(BigDecimal.valueOf(50.00), "Test deposit");
        when(walletService.deposit(eq(walletId), any(DepositRequest.class))).thenThrow(new WalletNotFoundException(walletId.toString()));

        mockMvc.perform(post("/api/v1/wallets/{walletId}/deposit", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code").value("WALLET_NOT_FOUND"));
    }

    @Test
    @DisplayName("Should withdraw successfully")
    void shouldWithdrawSuccessfully() throws Exception {
        var walletId = UUID.randomUUID();
        var request = new WithdrawRequest(BigDecimal.valueOf(30.00), "Test withdrawal");
        var response = new TransactionResponse(
                UUID.randomUUID(), walletId, "WITHDRAW", BigDecimal.valueOf(30.00),
                BigDecimal.valueOf(70.00), "Test withdrawal", LocalDateTime.now()
        );
        when(walletService.withdraw(eq(walletId), any(WithdrawRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/wallets/{walletId}/withdraw", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("WITHDRAW"))
                .andExpect(jsonPath("$.amount").value(30.00));
    }

    @Test
    @DisplayName("Should return bad request for invalid withdraw - negative amount")
    void shouldReturnBadRequestForInvalidWithdrawNegativeAmount() throws Exception {
        var walletId = UUID.randomUUID();
        var request = new WithdrawRequest(BigDecimal.valueOf(-10.00), "Test withdrawal");

        mockMvc.perform(post("/api/v1/wallets/{walletId}/withdraw", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.amount").value("Amount must be greater than 0"));
    }

    @Test
    @DisplayName("Should return 404 when withdrawing from non-existent wallet")
    void shouldReturn404WhenWithdrawingFromNonExistentWallet() throws Exception {
        var walletId = UUID.randomUUID();
        var request = new WithdrawRequest(BigDecimal.valueOf(30.00), "Test withdrawal");
        when(walletService.withdraw(eq(walletId), any(WithdrawRequest.class))).thenThrow(new WalletNotFoundException(walletId.toString()));

        mockMvc.perform(post("/api/v1/wallets/{walletId}/withdraw", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code").value("WALLET_NOT_FOUND"));
    }

    @Test
    @DisplayName("Should get transactions successfully")
    void shouldGetTransactionsSuccessfully() throws Exception {
        var walletId = UUID.randomUUID();
        var transactions = List.of(
                new TransactionResponse(UUID.randomUUID(), walletId, "DEPOSIT",
                        BigDecimal.valueOf(100.00), BigDecimal.valueOf(100.00),
                        "Deposit", LocalDateTime.now())
        );
        when(walletService.getTransactions(walletId)).thenReturn(transactions);

        mockMvc.perform(get("/api/v1/wallets/{walletId}/transactions", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"));
    }

    @Test
    @DisplayName("Should return 404 when getting transactions for non-existent wallet")
    void shouldReturn404WhenGettingTransactionsForNonExistentWallet() throws Exception {
        var walletId = UUID.randomUUID();
        when(walletService.getTransactions(walletId)).thenThrow(new WalletNotFoundException(walletId.toString()));

        mockMvc.perform(get("/api/v1/wallets/{walletId}/transactions", walletId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code").value("WALLET_NOT_FOUND"));
    }
}
