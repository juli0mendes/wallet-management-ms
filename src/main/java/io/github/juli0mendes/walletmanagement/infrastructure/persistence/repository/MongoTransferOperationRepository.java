package io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository;

import io.github.juli0mendes.walletmanagement.domain.model.TransferOperation;
import io.github.juli0mendes.walletmanagement.domain.model.TransferOperationStatus;
import io.github.juli0mendes.walletmanagement.domain.repository.TransferOperationRepository;
import io.github.juli0mendes.walletmanagement.infrastructure.persistence.mapper.TransferOperationMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class MongoTransferOperationRepository implements TransferOperationRepository {

    private final TransferOperationDataRepository transferOperationDataRepository;
    private final TransferOperationMapper transferOperationMapper;

    public MongoTransferOperationRepository(TransferOperationDataRepository transferOperationDataRepository,
                                           TransferOperationMapper transferOperationMapper) {
        this.transferOperationDataRepository = transferOperationDataRepository;
        this.transferOperationMapper = transferOperationMapper;
    }

    @Override
    public Optional<TransferOperation> findByOperationId(String operationId) {
        return transferOperationDataRepository.findByOperationId(operationId)
                .map(transferOperationMapper::toDomain);
    }

    @Override
    public TransferOperation save(TransferOperation transferOperation) {
        try {
            return transferOperationMapper.toDomain(
                    transferOperationDataRepository.save(transferOperationMapper.toDocument(transferOperation))
            );
        } catch (DataIntegrityViolationException ex) {
            throw ex;
        }
    }

    @Override
    public void updateStatus(String operationId, TransferOperationStatus status) {
        transferOperationDataRepository.findByOperationId(operationId)
                .ifPresent(document -> {
                    document.setStatus(status.name());
                    document.setUpdatedAt(LocalDateTime.now());
                    transferOperationDataRepository.save(document);
                });
    }
}
