package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
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

        if (sourceWallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance in source wallet");
        }

        BigDecimal newSourceBalance = sourceWallet.getBalance().subtract(amount);
        sourceWallet.setBalance(newSourceBalance);
        sourceWallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(sourceWallet);

        BigDecimal newDestBalance = destinationWallet.getBalance().add(amount);
        destinationWallet.setBalance(newDestBalance);
        destinationWallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(destinationWallet);

        LocalDateTime now = LocalDateTime.now();

        Transaction sourceTransaction = new Transaction(
                UUID.randomUUID(),
                sourceWalletId,
                "TRANSFER_OUT",
                amount,
                newSourceBalance,
                request.getDescription(),
                now
        );
        walletRepository.saveTransaction(sourceTransaction);

        Transaction destTransaction = new Transaction(
                UUID.randomUUID(),
                destinationWalletId,
                "TRANSFER_IN",
                amount,
                newDestBalance,
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
