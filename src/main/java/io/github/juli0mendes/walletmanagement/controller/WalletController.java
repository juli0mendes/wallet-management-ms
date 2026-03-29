package io.github.juli0mendes.walletmanagement.controller;

import io.github.juli0mendes.walletmanagement.dto.*;
import io.github.juli0mendes.walletmanagement.application.usecase.*;
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

    private final CreateWalletUseCase createWalletUseCase;
    private final GetWalletByIdUseCase getWalletByIdUseCase;
    private final GetBalanceUseCase getBalanceUseCase;
    private final GetTransactionsUseCase getTransactionsUseCase;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;

    public WalletController(CreateWalletUseCase createWalletUseCase,
                            GetWalletByIdUseCase getWalletByIdUseCase,
                            GetBalanceUseCase getBalanceUseCase,
                            GetTransactionsUseCase getTransactionsUseCase,
                            DepositUseCase depositUseCase,
                            WithdrawUseCase withdrawUseCase) {
        this.createWalletUseCase = createWalletUseCase;
        this.getWalletByIdUseCase = getWalletByIdUseCase;
        this.getBalanceUseCase = getBalanceUseCase;
        this.getTransactionsUseCase = getTransactionsUseCase;
        this.depositUseCase = depositUseCase;
        this.withdrawUseCase = withdrawUseCase;
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        WalletResponse response = createWalletUseCase.execute(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable UUID walletId) {
        WalletResponse response = getWalletByIdUseCase.execute(walletId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{walletId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(
            @PathVariable UUID walletId,
            @RequestParam(required = false) LocalDateTime at) {
        BalanceResponse response = getBalanceUseCase.execute(walletId, at);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable UUID walletId) {
        List<TransactionResponse> transactions = getTransactionsUseCase.execute(walletId);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable UUID walletId,
            @Valid @RequestBody DepositRequest request) {
        TransactionResponse response = depositUseCase.execute(walletId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable UUID walletId,
            @Valid @RequestBody WithdrawRequest request) {
        TransactionResponse response = withdrawUseCase.execute(walletId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
