package com.walletmanagement.infrastructure.persistence.repository;

import com.walletmanagement.domain.entity.Wallet;
import com.walletmanagement.domain.exception.InvalidWalletException;
import com.walletmanagement.infrastructure.persistence.document.WalletDocument;
import com.walletmanagement.infrastructure.persistence.mapper.WalletMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoWalletRepositoryTest {

    @Mock
    private SpringDataWalletMongoRepository springDataWalletMongoRepository;

    private MongoWalletRepository mongoWalletRepository;

    @BeforeEach
    void setUp() {
        mongoWalletRepository = new MongoWalletRepository(springDataWalletMongoRepository, new WalletMapper());
    }

    @Test
    void shouldSaveWallet() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", new BigDecimal("100.00"), LocalDateTime.now());
        WalletDocument walletDocument = new WalletMapper().toDocument(wallet);

        when(springDataWalletMongoRepository.save(any(WalletDocument.class))).thenReturn(walletDocument);

        Wallet savedWallet = mongoWalletRepository.save(wallet);

        assertEquals(wallet.getId(), savedWallet.getId());
        assertEquals(wallet.getOwnerName(), savedWallet.getOwnerName());
    }

    @Test
    void shouldFindWalletById() {
        Wallet wallet = new Wallet(UUID.randomUUID(), "John", new BigDecimal("100.00"), LocalDateTime.now());
        WalletDocument walletDocument = new WalletMapper().toDocument(wallet);

        when(springDataWalletMongoRepository.findById(wallet.getId().toString())).thenReturn(Optional.of(walletDocument));

        Optional<Wallet> result = mongoWalletRepository.findById(wallet.getId());

        assertTrue(result.isPresent());
        assertEquals(wallet.getId(), result.get().getId());
    }

    @Test
    void shouldReturnEmptyWhenWalletNotFound() {
        UUID id = UUID.randomUUID();
        when(springDataWalletMongoRepository.findById(id.toString())).thenReturn(Optional.empty());

        Optional<Wallet> result = mongoWalletRepository.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindAllWallets() {
        Wallet first = new Wallet(UUID.randomUUID(), "John", new BigDecimal("100.00"), LocalDateTime.now());
        Wallet second = new Wallet(UUID.randomUUID(), "Jane", new BigDecimal("200.00"), LocalDateTime.now());

        when(springDataWalletMongoRepository.findAll())
                .thenReturn(List.of(new WalletMapper().toDocument(first), new WalletMapper().toDocument(second)));

        List<Wallet> result = mongoWalletRepository.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void shouldDeleteWalletById() {
        UUID id = UUID.randomUUID();
        doNothing().when(springDataWalletMongoRepository).deleteById(id.toString());

        mongoWalletRepository.deleteById(id);

        verify(springDataWalletMongoRepository).deleteById(id.toString());
    }

    @Test
    void shouldRejectNullInputs() {
        assertThrows(InvalidWalletException.class, () -> mongoWalletRepository.save(null));
        assertThrows(InvalidWalletException.class, () -> mongoWalletRepository.findById(null));
        assertThrows(InvalidWalletException.class, () -> mongoWalletRepository.deleteById(null));
    }
}


