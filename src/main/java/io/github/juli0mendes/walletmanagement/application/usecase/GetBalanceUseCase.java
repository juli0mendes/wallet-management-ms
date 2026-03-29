package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.dto.BalanceResponse;
import io.github.juli0mendes.walletmanagement.exception.WalletNotFoundException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class GetBalanceUseCase {

    private final WalletRepository walletRepository;

    public GetBalanceUseCase(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public BalanceResponse execute(UUID walletId, LocalDateTime at) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId.toString()));

        BigDecimal balance = wallet.getBalance();

        if (at != null) {
            balance = calculateBalanceAtTime(walletId, at);
        }

        return new BalanceResponse(balance, "USD", LocalDateTime.now());
    }

    private BigDecimal calculateBalanceAtTime(UUID walletId, LocalDateTime at) {
        List<Transaction> transactions = walletRepository.findTransactionsByWalletIdAndCreatedAtBefore(walletId, at);
        BigDecimal balance = BigDecimal.ZERO;

        for (Transaction tx : transactions) {
            balance = tx.getBalanceAfter();
        }

        return balance;
    }
}
