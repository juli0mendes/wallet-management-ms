package com.walletmanagement.infrastructure.persistence.mapper;

import com.walletmanagement.domain.entity.Wallet;
import com.walletmanagement.domain.exception.InvalidWalletException;
import com.walletmanagement.infrastructure.persistence.document.WalletDocument;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WalletMapper {

    public WalletDocument toDocument(Wallet wallet) {
        if (wallet == null) {
            throw new InvalidWalletException("Wallet cannot be null");
        }

        return new WalletDocument(
                wallet.getId().toString(),
                wallet.getOwnerName(),
                wallet.getBalance(),
                wallet.getCreatedAt()
        );
    }

    public Wallet toDomain(WalletDocument document) {
        if (document == null) {
            throw new InvalidWalletException("Wallet document cannot be null");
        }

        return new Wallet(
                UUID.fromString(document.getId()),
                document.getOwnerName(),
                document.getBalance(),
                document.getCreatedAt()
        );
    }
}

