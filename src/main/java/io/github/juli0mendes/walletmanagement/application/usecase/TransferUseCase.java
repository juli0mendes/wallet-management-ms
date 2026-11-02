package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.TransactionType;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.TransactionRepository;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.dto.TransferRequest;
import io.github.juli0mendes.walletmanagement.dto.TransferResponse;
import io.github.juli0mendes.walletmanagement.exception.WalletNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TransferUseCase {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public TransferUseCase(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public TransferResponse execute(TransferRequest request) {
        UUID sourceWalletId = request.sourceWalletId();
        UUID destinationWalletId = request.destinationWalletId();
        BigDecimal amount = request.amount();

        Wallet sourceWallet = walletRepository.findById(sourceWalletId)
                .orElseThrow(() -> new WalletNotFoundException(sourceWalletId.toString()));

        Wallet destinationWallet = walletRepository.findById(destinationWalletId)
                .orElseThrow(() -> new WalletNotFoundException(destinationWalletId.toString()));

        sourceWallet.withdraw(amount);
        destinationWallet.deposit(amount);

        walletRepository.save(sourceWallet);
        walletRepository.save(destinationWallet);

        LocalDateTime now = LocalDateTime.now();

        Transaction sourceTransaction = new Transaction(
                UUID.randomUUID(),
                sourceWalletId,
                TransactionType.TRANSFER_OUT,
                amount,
                sourceWallet.getBalance(),
                request.description(),
                now
        );
        transactionRepository.save(sourceTransaction);

        Transaction destTransaction = new Transaction(
                UUID.randomUUID(),
                destinationWalletId,
                TransactionType.TRANSFER_IN,
                amount,
                destinationWallet.getBalance(),
                request.description(),
                now
        );
        transactionRepository.save(destTransaction);

        return new TransferResponse(
                UUID.randomUUID(),
                sourceWalletId,
                destinationWalletId,
                amount,
                "COMPLETED",
                request.description(),
                now
        );
    }
}
