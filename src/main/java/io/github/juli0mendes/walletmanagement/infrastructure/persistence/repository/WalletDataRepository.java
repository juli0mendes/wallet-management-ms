package io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository;

import io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.WalletDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface WalletDataRepository extends MongoRepository<WalletDocument, String> {
}

