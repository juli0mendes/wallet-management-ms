package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.TransactionType;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.TransactionRepository;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.dto.DepositRequest;
import io.github.juli0mendes.walletmanagement.dto.TransactionResponse;
import io.github.juli0mendes.walletmanagement.exception.WalletNotFoundException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DepositUseCase {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public DepositUseCase(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    public TransactionResponse execute(UUID walletId, DepositRequest request) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId.toString()));

        wallet.deposit(request.amount());

        Wallet savedWallet = walletRepository.save(wallet);

        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                walletId,
                TransactionType.DEPOSIT,
                request.amount(),
                savedWallet.getBalance(),
                request.description(),
                LocalDateTime.now()
        );

        Transaction savedTransaction = transactionRepository.save(transaction);

        return new TransactionResponse(
                savedTransaction.getId(),
                savedTransaction.getWalletId(),
                savedTransaction.getType().getValue(),
                savedTransaction.getAmount(),
                savedTransaction.getBalanceAfter(),
                savedTransaction.getDescription(),
                savedTransaction.getCreatedAt()
        );
    }
}
