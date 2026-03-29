package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.dto.DepositRequest;
import io.github.juli0mendes.walletmanagement.dto.TransactionResponse;
import io.github.juli0mendes.walletmanagement.exception.WalletNotFoundException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DepositUseCase {

    private final WalletRepository walletRepository;

    public DepositUseCase(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public TransactionResponse execute(UUID walletId, DepositRequest request) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId.toString()));

        BigDecimal newBalance = wallet.getBalance().add(request.getAmount());
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());

        Wallet savedWallet = walletRepository.save(wallet);

        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                walletId,
                "DEPOSIT",
                request.getAmount(),
                newBalance,
                request.getDescription(),
                LocalDateTime.now()
        );

        Transaction savedTransaction = walletRepository.saveTransaction(transaction);

        return new TransactionResponse(
                savedTransaction.getId(),
                savedTransaction.getWalletId(),
                savedTransaction.getType(),
                savedTransaction.getAmount(),
                savedTransaction.getBalanceAfter(),
                savedTransaction.getDescription(),
                savedTransaction.getCreatedAt()
        );
    }
}
