package io.github.juli0mendes.walletmanagement.infrastructure.persistence.mapper;

import io.github.juli0mendes.walletmanagement.domain.exception.InvalidWalletException;
import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.TransactionType;
import io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransactionDocument;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TransactionMapper {

    public TransactionDocument toDocument(Transaction transaction) {
        if (transaction == null) throw new InvalidWalletException("Transaction cannot be null");

        return new TransactionDocument(
                transaction.getId().toString(),
                transaction.getWalletId().toString(),
                transaction.getTransferId() != null ? transaction.getTransferId().toString() : null,
                transaction.getOperationId(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }

    public Transaction toDomain(TransactionDocument document) {
        if (document == null) throw new InvalidWalletException("TransactionDocument cannot be null");

        return new Transaction(
                UUID.fromString(document.getId()),
                UUID.fromString(document.getWalletId()),
                TransactionType.valueOf(document.getType()),
                document.getAmount(),
                document.getBalanceAfter(),
                document.getDescription(),
                document.getTransferId() != null ? UUID.fromString(document.getTransferId()) : null,
                document.getOperationId(),
                document.getCreatedAt()
        );
    }
}

