package io.github.juli0mendes.walletmanagement.infrastructure.persistence.mapper;

import io.github.juli0mendes.walletmanagement.domain.exception.InvalidWalletException;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.WalletDocument;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WalletMapper {

    public WalletDocument toDocument(Wallet wallet) {
        if (wallet == null) throw new InvalidWalletException("Wallet cannot be null");

        return new WalletDocument(
                wallet.getId().toString(),
                wallet.getOwnerName(),
                wallet.getOwnerEmail(),
                wallet.getBalance(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }

    public Wallet toDomain(WalletDocument document) {
        if (document == null) throw new InvalidWalletException("WalletDocument cannot be null");

        return new Wallet(
                UUID.fromString(document.getId()),
                document.getOwnerName(),
                document.getOwnerEmail(),
                document.getBalance(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}

