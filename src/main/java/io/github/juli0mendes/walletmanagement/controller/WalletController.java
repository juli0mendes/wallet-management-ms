package io.github.juli0mendes.walletmanagement.controller;

import io.github.juli0mendes.walletmanagement.dto.*;
import io.github.juli0mendes.walletmanagement.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        WalletResponse response = walletService.createWallet(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable UUID walletId) {
        WalletResponse response = walletService.getWallet(walletId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{walletId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(
            @PathVariable UUID walletId,
            @RequestParam(required = false) LocalDateTime at) {
        BalanceResponse response = walletService.getBalance(walletId, at);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable UUID walletId) {
        List<TransactionResponse> transactions = walletService.getTransactions(walletId);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable UUID walletId,
            @Valid @RequestBody DepositRequest request) {
        TransactionResponse response = walletService.deposit(walletId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable UUID walletId,
            @Valid @RequestBody WithdrawRequest request) {
        TransactionResponse response = walletService.withdraw(walletId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

