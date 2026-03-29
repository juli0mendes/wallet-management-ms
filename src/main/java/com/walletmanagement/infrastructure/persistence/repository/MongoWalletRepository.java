package com.walletmanagement.infrastructure.persistence.repository;

import com.walletmanagement.domain.entity.Wallet;
import com.walletmanagement.domain.exception.InvalidWalletException;
import com.walletmanagement.infrastructure.persistence.mapper.WalletMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MongoWalletRepository implements com.walletmanagement.domain.repository.WalletRepository {

    private final SpringDataWalletMongoRepository springDataWalletMongoRepository;
    private final WalletMapper walletMapper;

    public MongoWalletRepository(
            SpringDataWalletMongoRepository springDataWalletMongoRepository,
            WalletMapper walletMapper
    ) {
        this.springDataWalletMongoRepository = springDataWalletMongoRepository;
        this.walletMapper = walletMapper;
    }

    @Override
    public Wallet save(Wallet wallet) {
        if (wallet == null) {
            throw new InvalidWalletException("Wallet cannot be null");
        }

        var walletDocument = walletMapper.toDocument(wallet);
        var savedDocument = springDataWalletMongoRepository.save(walletDocument);
        return walletMapper.toDomain(savedDocument);
    }

    @Override
    public Optional<Wallet> findById(UUID id) {
        validateId(id);
        return springDataWalletMongoRepository.findById(id.toString())
                .map(walletMapper::toDomain);
    }

    @Override
    public List<Wallet> findAll() {
        return springDataWalletMongoRepository.findAll().stream()
                .map(walletMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        validateId(id);
        springDataWalletMongoRepository.deleteById(id.toString());
    }

    private void validateId(UUID id) {
        if (id == null) {
            throw new InvalidWalletException("Wallet id cannot be null");
        }
    }
}

