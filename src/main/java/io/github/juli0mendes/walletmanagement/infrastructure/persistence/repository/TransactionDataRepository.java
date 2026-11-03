package io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository;

import io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransactionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
public interface TransactionDataRepository extends MongoRepository<TransactionDocument, String> {
    List<TransactionDocument> findByWalletId(String walletId);
    List<TransactionDocument> findByWalletIdAndCreatedAtLessThanEqual(String walletId, LocalDateTime at);
    List<TransactionDocument> findByTransferId(String transferId);
}

