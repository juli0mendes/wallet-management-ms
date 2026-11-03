package io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository;

import io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransferOperationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TransferOperationDataRepository extends MongoRepository<TransferOperationDocument, String> {
    Optional<TransferOperationDocument> findByOperationId(String operationId);
}
