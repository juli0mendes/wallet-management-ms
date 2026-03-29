package com.walletmanagement.domain.repository;

import com.walletmanagement.domain.entity.Wallet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {

    Wallet save(Wallet wallet);

    Optional<Wallet> findById(UUID id);

    List<Wallet> findAll();

    void deleteById(UUID id);
}

