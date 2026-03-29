package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.TransactionType;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.dto.TransactionResponse;
import io.github.juli0mendes.walletmanagement.dto.WithdrawRequest;
import io.github.juli0mendes.walletmanagement.exception.WalletNotFoundException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class WithdrawUseCase {

    private final WalletRepository walletRepository;

    public WithdrawUseCase(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public TransactionResponse execute(UUID walletId, WithdrawRequest request) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId.toString()));

        wallet.withdraw(request.getAmount());

        Wallet savedWallet = walletRepository.save(wallet);

        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                walletId,
                TransactionType.WITHDRAW,
                request.getAmount(),
                savedWallet.getBalance(),
                request.getDescription(),
                LocalDateTime.now()
        );

        Transaction savedTransaction = walletRepository.saveTransaction(transaction);

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
