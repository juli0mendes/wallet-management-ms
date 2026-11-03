package io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository;

import io.github.juli0mendes.walletmanagement.domain.model.TransferOperation;
import io.github.juli0mendes.walletmanagement.domain.model.TransferOperationStatus;
import io.github.juli0mendes.walletmanagement.domain.repository.TransferOperationRepository;
import io.github.juli0mendes.walletmanagement.infrastructure.persistence.mapper.TransferOperationMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MongoTransferOperationRepository implements TransferOperationRepository {

    private final TransferOperationDataRepository transferOperationDataRepository;
    private final TransferOperationMapper transferOperationMapper;
    private final MongoTemplate mongoTemplate;
    private final Clock clock;

    @Value("${wallet.transfer.reconciliation.lease-duration:PT5M}")
    private Duration leaseDuration;

    @Value("${wallet.transfer.reconciliation.initiated-stale-after:PT5M}")
    private Duration initiatedStaleAfter;

    public MongoTransferOperationRepository(TransferOperationDataRepository transferOperationDataRepository,
                                           TransferOperationMapper transferOperationMapper,
                                           MongoTemplate mongoTemplate,
                                           Clock clock) {
        this.transferOperationDataRepository = transferOperationDataRepository;
        this.transferOperationMapper = transferOperationMapper;
        this.mongoTemplate = mongoTemplate;
        this.clock = clock == null ? Clock.systemUTC() : clock;
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
        LocalDateTime staleInitiatedThreshold = asOf.minus(initiatedStaleAfter);

        Criteria criteria = new Criteria().orOperator(
                Criteria.where("status").is(TransferOperationStatus.UNKNOWN_COMMIT_RESULT.name()),
                Criteria.where("status").is(TransferOperationStatus.RECONCILING.name())
                        .andOperator(Criteria.where("leaseExpiresAt").lt(asOf)),
                Criteria.where("status").is(TransferOperationStatus.INITIATED.name())
                        .andOperator(Criteria.where("updatedAt").lt(staleInitiatedThreshold))
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
    public long acquireExecutionOwnership(String operationId, String executionOwner, LocalDateTime leaseExpiresAt) {
        if (operationId == null || operationId.isBlank() || executionOwner == null || executionOwner.isBlank() || leaseExpiresAt == null) {
            return -1L;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        Query query = new Query(Criteria.where("operationId").is(operationId)
                .and("status").is(TransferOperationStatus.INITIATED.name())
                .andOperator(
                        new Criteria().orOperator(
                                Criteria.where("leaseOwner").exists(false),
                                Criteria.where("leaseOwner").is(executionOwner),
                                Criteria.where("leaseExpiresAt").lt(now)
                        )
                ));
        Update update = new Update()
                .set("leaseOwner", executionOwner)
                .set("leaseExpiresAt", leaseExpiresAt)
                .inc("fencingToken", 1)
                .set("updatedAt", now);

        io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransferOperationDocument document =
                mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true),
                        io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransferOperationDocument.class);
        return document == null ? -1L : document.getFencingToken();
    }

    @Override
    public boolean updateStatus(String operationId, TransferOperationStatus status) {
        return updateStatus(operationId, status, null);
    }

    @Override
    public boolean updateStatus(String operationId, TransferOperationStatus status, String expectedLeaseOwner) {
        if (operationId == null || operationId.isBlank() || status == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        List<TransferOperationStatus> allowedCurrentStatuses = switch (status) {
            case APPLIED -> List.of(TransferOperationStatus.INITIATED, TransferOperationStatus.RECONCILING);
            case REJECTED -> List.of(TransferOperationStatus.INITIATED);
            case UNKNOWN_COMMIT_RESULT -> List.of(TransferOperationStatus.INITIATED, TransferOperationStatus.RECONCILING);
            case RECONCILING -> List.of(TransferOperationStatus.UNKNOWN_COMMIT_RESULT);
            case INITIATED -> List.of();
        };

        if (allowedCurrentStatuses.isEmpty()) {
            return false;
        }

        if (status == TransferOperationStatus.APPLIED) {
            return false;
        }

        Criteria criteria = Criteria.where("operationId").is(operationId)
                .and("status").in(allowedCurrentStatuses.stream().map(Enum::name).toList());
        if (expectedLeaseOwner != null && !expectedLeaseOwner.isBlank()) {
            criteria = criteria.and("leaseOwner").is(expectedLeaseOwner);
        }

        Query query = new Query(criteria);
        Update update = new Update()
                .set("status", status.name())
                .set("updatedAt", now);

        if (status == TransferOperationStatus.RECONCILING) {
            update.set("leaseOwner", "system")
                    .set("leaseExpiresAt", now.plusMinutes(5));
        } else {
            update.set("leaseOwner", null)
                    .set("leaseExpiresAt", null);
        }

        return mongoTemplate.updateFirst(query, update,
                io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransferOperationDocument.class)
                .getModifiedCount() > 0;
    }

    @Override
    public boolean markReconciliationInProgress(String operationId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Query query = new Query(Criteria.where("operationId").is(operationId)
                .and("status").is(TransferOperationStatus.UNKNOWN_COMMIT_RESULT.name()));
        Update update = new Update()
                .set("status", TransferOperationStatus.RECONCILING.name())
                .set("updatedAt", now)
                .set("leaseOwner", "system")
                .set("leaseExpiresAt", now.plus(leaseDuration));

        return mongoTemplate.updateFirst(query, update,
                io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransferOperationDocument.class)
                .getModifiedCount() > 0;
    }

    @Override
    public boolean markExecutionInProgress(String operationId, String executionOwner, LocalDateTime leaseExpiresAt) {
        if (operationId == null || operationId.isBlank() || executionOwner == null || executionOwner.isBlank() || leaseExpiresAt == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        Query query = new Query(Criteria.where("operationId").is(operationId)
                .and("status").is(TransferOperationStatus.INITIATED.name())
                .and("leaseOwner").is(executionOwner));
        Update update = new Update()
                .set("leaseOwner", executionOwner)
                .set("leaseExpiresAt", leaseExpiresAt)
                .set("updatedAt", now);

        return mongoTemplate.updateFirst(query, update,
                io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransferOperationDocument.class)
                .getModifiedCount() > 0;
    }

    @Override
    public boolean acquireReconciliationLease(String operationId, String leaseOwner, LocalDateTime leaseExpiresAt) {
        return acquireReconciliationLeaseToken(operationId, leaseOwner, leaseExpiresAt) >= 0L;
    }

    @Override
    public long acquireReconciliationLeaseToken(String operationId, String leaseOwner, LocalDateTime leaseExpiresAt) {
        if (leaseOwner == null || leaseOwner.isBlank() || leaseExpiresAt == null) {
            return -1L;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        Criteria statusCriteria = new Criteria().orOperator(
                Criteria.where("status").is(TransferOperationStatus.INITIATED.name())
                        .and("updatedAt").lt(now.minus(initiatedStaleAfter))
                        .and("leaseOwner").exists(false),
                Criteria.where("status").is(TransferOperationStatus.UNKNOWN_COMMIT_RESULT.name()),
                Criteria.where("status").is(TransferOperationStatus.RECONCILING.name())
        );
        Criteria leaseCriteria = new Criteria().orOperator(
                Criteria.where("leaseOwner").exists(false),
                Criteria.where("leaseOwner").is(leaseOwner),
                Criteria.where("leaseExpiresAt").lt(now)
        );
        Criteria criteria = Criteria.where("operationId").is(operationId)
                .andOperator(statusCriteria, leaseCriteria);

        Update update = new Update()
                .set("status", TransferOperationStatus.RECONCILING.name())
                .set("leaseOwner", leaseOwner)
                .set("leaseExpiresAt", leaseExpiresAt)
                .inc("fencingToken", 1)
                .set("updatedAt", now);

        io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransferOperationDocument document =
                mongoTemplate.findAndModify(new Query(criteria), update,
                        FindAndModifyOptions.options().returnNew(true),
                        io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransferOperationDocument.class);
        return document == null ? -1L : document.getFencingToken();
    }

    @Override
    public boolean confirmFinancialExecution(String operationId, String executionOwner, long fencingToken) {
        if (operationId == null || operationId.isBlank() || executionOwner == null || executionOwner.isBlank()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        Query query = new Query(Criteria.where("operationId").is(operationId)
                .and("status").in(TransferOperationStatus.INITIATED.name(), TransferOperationStatus.RECONCILING.name())
                .and("leaseOwner").is(executionOwner)
                .and("leaseExpiresAt").gt(now)
                .and("fencingToken").is(fencingToken));
        Update update = new Update()
                .set("status", TransferOperationStatus.APPLIED.name())
                .set("leaseOwner", null)
                .set("leaseExpiresAt", null)
                .set("updatedAt", now);

        return mongoTemplate.updateFirst(query, update,
                io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransferOperationDocument.class)
                .getModifiedCount() > 0;
    }

    @Override
    public boolean finalizeReconciliation(String operationId, TransferOperationStatus status, String leaseOwner) {
        return finalizeReconciliation(operationId, status, leaseOwner, 0L);
    }

    @Override
    public boolean finalizeReconciliation(String operationId, TransferOperationStatus status, String leaseOwner, long fencingToken) {
        if (status == null || leaseOwner == null || leaseOwner.isBlank()) {
            return false;
        }

        if (!TransferOperationStatus.canTransition(TransferOperationStatus.RECONCILING, status)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        Criteria criteria = Criteria.where("operationId").is(operationId)
                .and("status").is(TransferOperationStatus.RECONCILING.name())
                .and("leaseOwner").is(leaseOwner)
                .and("leaseExpiresAt").gt(now);
        if (fencingToken > 0L) {
            criteria = criteria.and("fencingToken").is(fencingToken);
        }
        Query query = new Query(criteria);
        Update update = new Update()
                .set("status", status.name())
                .set("leaseOwner", null)
                .set("leaseExpiresAt", null)
                .set("updatedAt", now);

        return mongoTemplate.updateFirst(query, update,
                io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransferOperationDocument.class)
                .getModifiedCount() > 0;
    }
}
