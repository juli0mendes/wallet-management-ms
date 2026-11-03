package io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository;

import io.github.juli0mendes.walletmanagement.domain.model.TransferOperation;
import io.github.juli0mendes.walletmanagement.domain.model.TransferOperationStatus;
import io.github.juli0mendes.walletmanagement.domain.repository.TransferOperationRepository;
import io.github.juli0mendes.walletmanagement.infrastructure.persistence.mapper.TransferOperationMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MongoTransferOperationRepository implements TransferOperationRepository {

    private final TransferOperationDataRepository transferOperationDataRepository;
    private final TransferOperationMapper transferOperationMapper;
    private final MongoTemplate mongoTemplate;

    public MongoTransferOperationRepository(TransferOperationDataRepository transferOperationDataRepository,
                                           TransferOperationMapper transferOperationMapper,
                                           MongoTemplate mongoTemplate) {
        this.transferOperationDataRepository = transferOperationDataRepository;
        this.transferOperationMapper = transferOperationMapper;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<TransferOperation> findByOperationId(String operationId) {
        return transferOperationDataRepository.findByOperationId(operationId)
                .map(transferOperationMapper::toDomain);
    }

    @Override
    public List<TransferOperation> findByStatus(TransferOperationStatus status, int limit) {
        Query query = new Query(Criteria.where("status").is(status.name()))
                .with(Sort.by(Sort.Direction.ASC, "updatedAt"))
                .limit(limit);
        return mongoTemplate.find(query, io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransferOperationDocument.class)
                .stream()
                .map(transferOperationMapper::toDomain)
                .toList();
    }

    @Override
    public List<TransferOperation> findPendingReconciliation(int limit, LocalDateTime asOf) {
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("status").is(TransferOperationStatus.UNKNOWN_COMMIT_RESULT.name()),
                Criteria.where("status").is(TransferOperationStatus.RECONCILING.name())
                        .andOperator(Criteria.where("leaseExpiresAt").lt(asOf))
        );

        Query query = new Query(criteria)
                .with(Sort.by(Sort.Direction.ASC, "updatedAt"))
                .limit(limit);
        return mongoTemplate.find(query, io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransferOperationDocument.class)
                .stream()
                .map(transferOperationMapper::toDomain)
                .toList();
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
                    document.setLeaseOwner(null);
                    document.setLeaseExpiresAt(null);
                    transferOperationDataRepository.save(document);
                });
    }

    @Override
    public boolean markReconciliationInProgress(String operationId) {
        Query query = new Query(Criteria.where("operationId").is(operationId)
                .and("status").is(TransferOperationStatus.UNKNOWN_COMMIT_RESULT.name()));
        Update update = new Update()
                .set("status", TransferOperationStatus.RECONCILING.name())
                .set("updatedAt", LocalDateTime.now())
                .set("leaseOwner", "system")
                .set("leaseExpiresAt", LocalDateTime.now().plusMinutes(5));

        return mongoTemplate.updateFirst(query, update,
                io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransferOperationDocument.class)
                .getModifiedCount() > 0;
    }

    @Override
    public boolean acquireReconciliationLease(String operationId, String leaseOwner, LocalDateTime leaseExpiresAt) {
        if (leaseOwner == null || leaseOwner.isBlank() || leaseExpiresAt == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        Criteria criteria = Criteria.where("operationId").is(operationId)
                .and("status").in(TransferOperationStatus.UNKNOWN_COMMIT_RESULT.name(), TransferOperationStatus.RECONCILING.name())
                .andOperator(new Criteria().orOperator(
                        Criteria.where("leaseOwner").exists(false),
                        Criteria.where("leaseOwner").is(leaseOwner),
                        Criteria.where("leaseExpiresAt").lt(now)
                ));

        Update update = new Update()
                .set("status", TransferOperationStatus.RECONCILING.name())
                .set("leaseOwner", leaseOwner)
                .set("leaseExpiresAt", leaseExpiresAt)
                .set("updatedAt", now);

        return mongoTemplate.updateFirst(new Query(criteria), update,
                io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransferOperationDocument.class)
                .getModifiedCount() > 0;
    }

    @Override
    public boolean finalizeReconciliation(String operationId, TransferOperationStatus status, String leaseOwner) {
        if (status == null || leaseOwner == null || leaseOwner.isBlank()) {
            return false;
        }

        Query query = new Query(Criteria.where("operationId").is(operationId)
                .and("status").is(TransferOperationStatus.RECONCILING.name())
                .and("leaseOwner").is(leaseOwner));
        Update update = new Update()
                .set("status", status.name())
                .set("leaseOwner", null)
                .set("leaseExpiresAt", null)
                .set("updatedAt", LocalDateTime.now());

        return mongoTemplate.updateFirst(query, update,
                io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransferOperationDocument.class)
                .getModifiedCount() > 0;
    }
}
