package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.TransactionType;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.dto.TransferRequest;
import io.github.juli0mendes.walletmanagement.dto.TransferResponse;
import io.github.juli0mendes.walletmanagement.exception.WalletNotFoundException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TransferUseCase {

    private final WalletRepository walletRepository;

    public TransferUseCase(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public TransferResponse execute(TransferRequest request) {
        UUID sourceWalletId = request.getSourceWalletId();
        UUID destinationWalletId = request.getDestinationWalletId();
        BigDecimal amount = request.getAmount();

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
                request.getDescription(),
                now
        );
        walletRepository.saveTransaction(sourceTransaction);

        Transaction destTransaction = new Transaction(
                UUID.randomUUID(),
                destinationWalletId,
                TransactionType.TRANSFER_IN,
                amount,
                destinationWallet.getBalance(),
                request.getDescription(),
                now
        );
        walletRepository.saveTransaction(destTransaction);

        return new TransferResponse(
                UUID.randomUUID(),
                sourceWalletId,
                destinationWalletId,
                amount,
                "COMPLETED",
                request.getDescription(),
                now
        );
    }
}
