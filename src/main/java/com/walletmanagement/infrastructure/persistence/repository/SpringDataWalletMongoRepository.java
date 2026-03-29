package com.walletmanagement.infrastructure.persistence.repository;

import com.walletmanagement.infrastructure.persistence.document.WalletDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataWalletMongoRepository extends MongoRepository<WalletDocument, String> {
}

