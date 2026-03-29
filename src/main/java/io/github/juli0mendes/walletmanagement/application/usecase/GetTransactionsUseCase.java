package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.repository.TransactionRepository;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.dto.TransactionResponse;
import io.github.juli0mendes.walletmanagement.exception.WalletNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GetTransactionsUseCase {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public GetTransactionsUseCase(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionResponse> execute(UUID walletId) {
        walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId.toString()));

        List<Transaction> transactions = transactionRepository.findByWalletId(walletId);

        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse mapToResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getWalletId(),
                tx.getType().getValue(),
                tx.getAmount(),
                tx.getBalanceAfter(),
                tx.getDescription(),
                tx.getCreatedAt()
        );
    }
}
