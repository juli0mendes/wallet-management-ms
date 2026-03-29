package io.github.juli0mendes.walletmanagement.controller;

import io.github.juli0mendes.walletmanagement.dto.*;
import io.github.juli0mendes.walletmanagement.application.usecase.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private static final Logger log = LoggerFactory.getLogger(WalletController.class);

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
        log.info("Creating wallet for ownerName={}", request.ownerName());
        WalletResponse response = createWalletUseCase.execute(request);
        log.info("Wallet created successfully walletId={}", response.walletId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable UUID walletId) {
        log.debug("Fetching wallet details walletId={}", walletId);
        WalletResponse response = getWalletByIdUseCase.execute(walletId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{walletId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(
            @PathVariable UUID walletId,
            @RequestParam(required = false) LocalDateTime at) {
        log.debug("Fetching balance walletId={} at={}", walletId, at);
        BalanceResponse response = getBalanceUseCase.execute(walletId, at);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable UUID walletId) {
        log.debug("Fetching transactions walletId={}", walletId);
        List<TransactionResponse> transactions = getTransactionsUseCase.execute(walletId);
        log.debug("Transactions fetched walletId={} count={}", walletId, transactions.size());
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable UUID walletId,
            @Valid @RequestBody DepositRequest request) {
        log.info("Deposit requested walletId={} amount={}", walletId, request.amount());
        TransactionResponse response = depositUseCase.execute(walletId, request);
        log.info("Deposit completed walletId={} transactionId={} balanceAfter={}",
                walletId, response.transactionId(), response.balanceAfter());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable UUID walletId,
            @Valid @RequestBody WithdrawRequest request) {
        log.info("Withdraw requested walletId={} amount={}", walletId, request.amount());
        TransactionResponse response = withdrawUseCase.execute(walletId, request);
        log.info("Withdraw completed walletId={} transactionId={} balanceAfter={}",
                walletId, response.transactionId(), response.balanceAfter());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
