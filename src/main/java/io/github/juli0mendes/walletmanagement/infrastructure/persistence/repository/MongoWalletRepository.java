package io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository;

import io.github.juli0mendes.walletmanagement.domain.exception.InvalidWalletException;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.infrastructure.persistence.mapper.WalletMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MongoWalletRepository implements WalletRepository {

    private final WalletDataRepository walletDataRepository;
    private final WalletMapper walletMapper;

    public MongoWalletRepository(
            WalletDataRepository walletDataRepository,
            WalletMapper walletMapper) {
        this.walletDataRepository = walletDataRepository;
        this.walletMapper = walletMapper;
    }

    @Override
    public Optional<Wallet> findById(UUID id) {
        validateId(id);
        return walletDataRepository.findById(id.toString()).map(walletMapper::toDomain);
    }

    @Override
    public Wallet save(Wallet wallet) {
        if (wallet == null) throw new InvalidWalletException("Wallet cannot be null");
        return walletMapper.toDomain(walletDataRepository.save(walletMapper.toDocument(wallet)));
    }

    @Override
    public List<Wallet> findAll() {
        return walletDataRepository.findAll().stream().map(walletMapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        validateId(id);
        walletDataRepository.deleteById(id.toString());
    }


    private void validateId(UUID id) {
        if (id == null) throw new InvalidWalletException("Id cannot be null");
    }
}

