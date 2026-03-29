package com.walletmanagement.infrastructure.persistence.mapper;

import com.walletmanagement.domain.entity.Wallet;
import com.walletmanagement.domain.exception.InvalidWalletException;
import com.walletmanagement.infrastructure.persistence.document.WalletDocument;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WalletMapperTest {

    private final WalletMapper walletMapper = new WalletMapper();

    @Test
    void shouldMapDomainToDocument() {
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        Wallet wallet = new Wallet(id, "John Doe", new BigDecimal("10.00"), createdAt);

        WalletDocument document = walletMapper.toDocument(wallet);

        assertEquals(id.toString(), document.getId());
        assertEquals("John Doe", document.getOwnerName());
        assertEquals(new BigDecimal("10.00"), document.getBalance());
        assertEquals(createdAt, document.getCreatedAt());
    }

    @Test
    void shouldMapDocumentToDomain() {
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        WalletDocument document = new WalletDocument(id.toString(), "John Doe", new BigDecimal("10.00"), createdAt);

        Wallet wallet = walletMapper.toDomain(document);

        assertEquals(id, wallet.getId());
        assertEquals("John Doe", wallet.getOwnerName());
        assertEquals(new BigDecimal("10.00"), wallet.getBalance());
        assertEquals(createdAt, wallet.getCreatedAt());
    }

    @Test
    void shouldRejectNullInputs() {
        assertThrows(InvalidWalletException.class, () -> walletMapper.toDocument(null));
        assertThrows(InvalidWalletException.class, () -> walletMapper.toDomain(null));
    }
}

