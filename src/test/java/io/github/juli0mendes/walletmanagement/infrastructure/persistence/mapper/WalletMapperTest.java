package io.github.juli0mendes.walletmanagement.infrastructure.persistence.mapper;

import io.github.juli0mendes.walletmanagement.domain.exception.InvalidWalletException;
import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.TransactionType;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransactionDocument;
import io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.WalletDocument;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WalletMapperTest {

    private final WalletMapper walletMapper = new WalletMapper();
    private final TransactionMapper transactionMapper = new TransactionMapper();

    @Test
    void shouldMapWalletDomainToDocument() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Wallet wallet = new Wallet(id, "John Doe", "john@example.com", new BigDecimal("10.00"), now, now);

        WalletDocument document = walletMapper.toDocument(wallet);

        assertEquals(id.toString(), document.getId());
        assertEquals("John Doe", document.getOwnerName());
        assertEquals("john@example.com", document.getOwnerEmail());
        assertEquals(new BigDecimal("10.00"), document.getBalance());
        assertEquals(now, document.getCreatedAt());
        assertEquals(now, document.getUpdatedAt());
    }

    @Test
    void shouldMapWalletDocumentToDomain() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        WalletDocument document = new WalletDocument(id.toString(), "John Doe", "john@example.com",
                new BigDecimal("10.00"), now, now);

        Wallet wallet = walletMapper.toDomain(document);

        assertEquals(id, wallet.getId());
        assertEquals("John Doe", wallet.getOwnerName());
        assertEquals("john@example.com", wallet.getOwnerEmail());
        assertEquals(new BigDecimal("10.00"), wallet.getBalance());
        assertEquals(now, wallet.getCreatedAt());
        assertEquals(now, wallet.getUpdatedAt());
    }

    @Test
    void shouldRejectNullWallet() {
        assertThrows(InvalidWalletException.class, () -> walletMapper.toDocument(null));
        assertThrows(InvalidWalletException.class, () -> walletMapper.toDomain(null));
    }

    @Test
    void shouldMapTransactionDomainToDocument() {
        UUID id = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Transaction transaction = new Transaction(id, walletId, TransactionType.DEPOSIT,
                new BigDecimal("100.00"), new BigDecimal("100.00"), "deposit", now);

        TransactionDocument document = transactionMapper.toDocument(transaction);

        assertEquals(id.toString(), document.getId());
        assertEquals(walletId.toString(), document.getWalletId());
        assertEquals("DEPOSIT", document.getType());
        assertEquals(new BigDecimal("100.00"), document.getAmount());
        assertEquals(now, document.getCreatedAt());
    }

    @Test
    void shouldMapTransactionDocumentToDomain() {
        UUID id = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        TransactionDocument document = new TransactionDocument(id.toString(), walletId.toString(),
                "WITHDRAW", new BigDecimal("50.00"), new BigDecimal("50.00"), "withdraw", now);

        Transaction transaction = transactionMapper.toDomain(document);

        assertEquals(id, transaction.getId());
        assertEquals(walletId, transaction.getWalletId());
        assertEquals(TransactionType.WITHDRAW, transaction.getType());
    }

    @Test
    void shouldRejectNullTransaction() {
        assertThrows(InvalidWalletException.class, () -> transactionMapper.toDocument(null));
        assertThrows(InvalidWalletException.class, () -> transactionMapper.toDomain(null));
    }
}

