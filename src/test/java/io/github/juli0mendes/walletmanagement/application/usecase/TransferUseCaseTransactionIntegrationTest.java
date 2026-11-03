package io.github.juli0mendes.walletmanagement.application.usecase;

import io.github.juli0mendes.walletmanagement.application.service.TransferOperationReconciliationService;
import io.github.juli0mendes.walletmanagement.domain.exception.DuplicateTransferOperationException;
import io.github.juli0mendes.walletmanagement.domain.exception.InsufficientBalanceException;
import io.github.juli0mendes.walletmanagement.domain.model.TransferOperation;
import io.github.juli0mendes.walletmanagement.domain.model.TransferOperationStatus;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.TransferOperationRepository;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.dto.TransferRequest;
import io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransferOperationDocument;
import io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository.TransactionDataRepository;
import io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository.TransferOperationDataRepository;
import io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository.WalletDataRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.mongodb.MongoException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(properties = {
        "de.flapdoodle.mongodb.embedded.enabled=false",
        "spring.autoconfigure.exclude=de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration"
})
@Testcontainers
class TransferUseCaseTransactionIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer =
            new MongoDBContainer(DockerImageName.parse("mongo:7.0"))
                    .withCommand("--replSet", "rs0", "--bind_ip_all");

    @DynamicPropertySource
    static void configureMongoDB(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.data.mongodb.uri",
                () -> mongoDBContainer.getReplicaSetUrl("wallet_management_test")
        );
    }

    @BeforeAll
    static void initializeReplicaSet() throws Exception {
        mongoDBContainer.execInContainer(
                "mongosh",
                "--eval",
                "rs.initiate({_id: 'rs0', members: [{_id: 0, host: 'localhost:27017'}]})"
        );
    }

    @Autowired
    private TransferUseCase transferUseCase;

    @SpyBean
    private TransferOperationRepository transferOperationRepositorySpy;

    @Autowired
    private TransferOperationReconciliationService reconciliationService;

    @Autowired
    private TransferOperationRepository transferOperationRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletDataRepository walletDataRepository;

    @Autowired
    private TransactionDataRepository transactionDataRepository;

    @Autowired
    private TransferOperationDataRepository transferOperationDataRepository;

    @Autowired
    private MongoTransactionManager mongoTransactionManager;

    @BeforeEach
    void cleanDatabase() {
        transactionDataRepository.deleteAll();
        walletDataRepository.deleteAll();
        transferOperationDataRepository.deleteAll();
    }

    @Test
    @DisplayName("Should persist a successful transfer across both wallets and both transaction records")
    void shouldPersistSuccessfulTransferAcrossWalletsAndTransactions() {
        var now = LocalDateTime.now();
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();

        walletRepository.save(new Wallet(sourceWalletId, "John Doe", "john@example.com", new BigDecimal("200.00"), now, now));
        walletRepository.save(new Wallet(destinationWalletId, "Jane Doe", "jane@example.com", new BigDecimal("50.00"), now, now));

        var response = transferUseCase.execute(new TransferRequest(
                sourceWalletId,
                destinationWalletId,
                new BigDecimal("75.00"),
                "successful transfer"
        ));

        var sourceWallet = walletRepository.findById(sourceWalletId).orElseThrow();
        var destinationWallet = walletRepository.findById(destinationWalletId).orElseThrow();

        assertEquals("COMPLETED", response.getStatus());
        assertEquals(new BigDecimal("125.00"), sourceWallet.getBalance());
        assertEquals(new BigDecimal("125.00"), destinationWallet.getBalance());
        assertEquals(1, transactionDataRepository.findByWalletId(sourceWalletId.toString()).size());
        assertEquals(1, transactionDataRepository.findByWalletId(destinationWalletId.toString()).size());
    }

    @Test
    @DisplayName("Should not persist any changes when the source balance is insufficient")
    void shouldNotPersistAnyChangesWhenBalanceIsInsufficient() {
        var now = LocalDateTime.now();
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();

        walletRepository.save(new Wallet(sourceWalletId, "John Doe", "john@example.com", new BigDecimal("50.00"), now, now));
        walletRepository.save(new Wallet(destinationWalletId, "Jane Doe", "jane@example.com", new BigDecimal("25.00"), now, now));

        assertThrows(InsufficientBalanceException.class, () -> transferUseCase.execute(new TransferRequest(
                sourceWalletId,
                destinationWalletId,
                new BigDecimal("75.00"),
                "insufficient balance transfer"
        )));

        var sourceWallet = walletRepository.findById(sourceWalletId).orElseThrow();
        var destinationWallet = walletRepository.findById(destinationWalletId).orElseThrow();
        assertEquals(new BigDecimal("50.00"), sourceWallet.getBalance());
        assertEquals(new BigDecimal("25.00"), destinationWallet.getBalance());
        assertTrue(transactionDataRepository.findByWalletId(sourceWalletId.toString()).isEmpty());
        assertTrue(transactionDataRepository.findByWalletId(destinationWalletId.toString()).isEmpty());
    }

    @Test
    @DisplayName("Should rollback after a failure injected after the first write in a real Mongo transaction")
    void shouldRollbackAfterFailureInjectedAfterFirstWrite() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var now = LocalDateTime.now();

        walletRepository.save(new Wallet(sourceWalletId, "John Doe", "john@example.com", new BigDecimal("200.00"), now, now));
        walletRepository.save(new Wallet(destinationWalletId, "Jane Doe", "jane@example.com", new BigDecimal("50.00"), now, now));

        TransactionTemplate template = new TransactionTemplate(mongoTransactionManager);

        assertThrows(RuntimeException.class, () -> template.execute(status -> {
            var sourceWallet = walletRepository.findById(sourceWalletId).orElseThrow();
            var destinationWallet = walletRepository.findById(destinationWalletId).orElseThrow();

            sourceWallet.withdraw(new BigDecimal("75.00"));
            destinationWallet.deposit(new BigDecimal("75.00"));
            walletRepository.save(sourceWallet);
            walletRepository.save(destinationWallet);

            throw new RuntimeException("injected failure after first write");
        }));

        var sourceWallet = walletRepository.findById(sourceWalletId).orElseThrow();
        var destinationWallet = walletRepository.findById(destinationWalletId).orElseThrow();

        assertEquals(new BigDecimal("200.00"), sourceWallet.getBalance());
        assertEquals(new BigDecimal("50.00"), destinationWallet.getBalance());
        assertTrue(transactionDataRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Should preserve total balance consistency under concurrent transfers")
    void shouldPreserveFinancialConsistencyUnderConcurrentTransfers() throws Exception {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var now = LocalDateTime.now();

        walletRepository.save(new Wallet(sourceWalletId, "John Doe", "john@example.com", new BigDecimal("1000.00"), now, now));
        walletRepository.save(new Wallet(destinationWalletId, "Jane Doe", "jane@example.com", new BigDecimal("100.00"), now, now));

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        List<Callable<Void>> tasks = List.of(
                () -> {
                    try {
                        transferUseCase.execute(new TransferRequest(sourceWalletId, destinationWalletId, new BigDecimal("150.00"), "concurrent-1"));
                    } catch (Exception ignored) {
                        // concurrency conflicts are expected and should fail closed
                    }
                    return null;
                },
                () -> {
                    try {
                        transferUseCase.execute(new TransferRequest(sourceWalletId, destinationWalletId, new BigDecimal("150.00"), "concurrent-2"));
                    } catch (Exception ignored) {
                        // concurrency conflicts are expected and should fail closed
                    }
                    return null;
                },
                () -> {
                    try {
                        transferUseCase.execute(new TransferRequest(sourceWalletId, destinationWalletId, new BigDecimal("150.00"), "concurrent-3"));
                    } catch (Exception ignored) {
                        // concurrency conflicts are expected and should fail closed
                    }
                    return null;
                },
                () -> {
                    try {
                        transferUseCase.execute(new TransferRequest(sourceWalletId, destinationWalletId, new BigDecimal("150.00"), "concurrent-4"));
                    } catch (Exception ignored) {
                        // concurrency conflicts are expected and should fail closed
                    }
                    return null;
                }
        );

        List<Future<Void>> futures = executorService.invokeAll(tasks);
        executorService.shutdown();
        for (Future<Void> future : futures) {
            future.get();
        }

        var sourceWallet = walletRepository.findById(sourceWalletId).orElseThrow();
        var destinationWallet = walletRepository.findById(destinationWalletId).orElseThrow();

        assertTrue(sourceWallet.getBalance().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(destinationWallet.getBalance().compareTo(BigDecimal.ZERO) >= 0);
        assertEquals(new BigDecimal("1100.00"), sourceWallet.getBalance().add(destinationWallet.getBalance()));
    }

    @Test
    @DisplayName("Should fail closed when a transaction throws after a write and no success state is claimed")
    void shouldFailClosedWhenTransactionThrowsAfterWrite() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var now = LocalDateTime.now();

        walletRepository.save(new Wallet(sourceWalletId, "John Doe", "john@example.com", new BigDecimal("200.00"), now, now));
        walletRepository.save(new Wallet(destinationWalletId, "Jane Doe", "jane@example.com", new BigDecimal("50.00"), now, now));

        TransactionTemplate template = new TransactionTemplate(mongoTransactionManager);

        assertThrows(RuntimeException.class, () -> template.execute(status -> {
            var sourceWallet = walletRepository.findById(sourceWalletId).orElseThrow();
            var destinationWallet = walletRepository.findById(destinationWalletId).orElseThrow();

            sourceWallet.withdraw(new BigDecimal("40.00"));
            destinationWallet.deposit(new BigDecimal("40.00"));
            walletRepository.save(sourceWallet);
            walletRepository.save(destinationWallet);

            throw new RuntimeException("simulated commit-stage exception");
        }));

        var sourceWallet = walletRepository.findById(sourceWalletId).orElseThrow();
        var destinationWallet = walletRepository.findById(destinationWalletId).orElseThrow();
        assertEquals(new BigDecimal("200.00"), sourceWallet.getBalance());
        assertEquals(new BigDecimal("50.00"), destinationWallet.getBalance());
        assertTrue(transactionDataRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Should return the same result when the same operation id is retried")
    void shouldReturnSameResultWhenSameOperationIdIsRetried() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var operationId = "idempotent-success";

        walletRepository.save(new Wallet(sourceWalletId, "John Doe", "john@example.com", new BigDecimal("200.00"), now, now));
        walletRepository.save(new Wallet(destinationWalletId, "Jane Doe", "jane@example.com", new BigDecimal("50.00"), now, now));

        var firstResponse = transferUseCase.execute(new TransferRequest(
                sourceWalletId,
                destinationWalletId,
                new BigDecimal("25.00"),
                "idempotent transfer",
                operationId
        ));

        var repeatedResponse = transferUseCase.execute(new TransferRequest(
                sourceWalletId,
                destinationWalletId,
                new BigDecimal("25.00"),
                "idempotent transfer",
                operationId
        ));

        assertEquals("COMPLETED", firstResponse.getStatus());
        assertEquals("COMPLETED", repeatedResponse.getStatus());
        assertEquals(new BigDecimal("175.00"), walletRepository.findById(sourceWalletId).orElseThrow().getBalance());
        assertEquals(new BigDecimal("75.00"), walletRepository.findById(destinationWalletId).orElseThrow().getBalance());
        assertEquals(1, transactionDataRepository.findByWalletId(sourceWalletId.toString()).size());
        assertEquals(1, transactionDataRepository.findByWalletId(destinationWalletId.toString()).size());
    }

    @Test
    @DisplayName("Should reject a reused operation id with a different payload")
    void shouldRejectReusedOperationIdWithDifferentPayload() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var operationId = "idempotent-conflict";

        walletRepository.save(new Wallet(sourceWalletId, "John Doe", "john@example.com", new BigDecimal("200.00"), now, now));
        walletRepository.save(new Wallet(destinationWalletId, "Jane Doe", "jane@example.com", new BigDecimal("50.00"), now, now));

        transferUseCase.execute(new TransferRequest(
                sourceWalletId,
                destinationWalletId,
                new BigDecimal("25.00"),
                "first transfer",
                operationId
        ));

        assertThrows(DuplicateTransferOperationException.class, () -> transferUseCase.execute(new TransferRequest(
                sourceWalletId,
                destinationWalletId,
                new BigDecimal("40.00"),
                "different payload",
                operationId
        )));
    }

    @Test
    @DisplayName("Should reconcile an indeterminate operation when financial evidence exists")
    void shouldReconcileIndeterminateOperationWhenFinancialEvidenceExists() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var amount = new BigDecimal("40.00");
        var description = "reconcile transfer";
        var operationId = "reconcile-known";
        var transferId = UUID.randomUUID();
        var now = LocalDateTime.now();

        transferOperationRepository.save(new TransferOperation(
                UUID.randomUUID(),
                operationId,
                transferId,
                sourceWalletId,
                destinationWalletId,
                amount,
                "payload-hash",
                TransferOperationStatus.UNKNOWN_COMMIT_RESULT,
                description,
                now,
                now
        ));

        transactionDataRepository.save(new io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransactionDocument(
                UUID.randomUUID().toString(),
                sourceWalletId.toString(),
                transferId.toString(),
                operationId,
                "TRANSFER_OUT",
                amount,
                new BigDecimal("160.00"),
                description,
                now
        ));
        transactionDataRepository.save(new io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransactionDocument(
                UUID.randomUUID().toString(),
                destinationWalletId.toString(),
                transferId.toString(),
                operationId,
                "TRANSFER_IN",
                amount,
                new BigDecimal("140.00"),
                description,
                now
        ));

        var operation = transferOperationRepository.findByOperationId(operationId).orElseThrow();
        assertTrue(reconciliationService.reconcileOperation(operation));
        assertEquals(TransferOperationStatus.APPLIED, transferOperationRepository.findByOperationId(operationId).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Should not recover a recent initiated operation while it may still be active")
    void shouldNotRecoverRecentInitiatedOperation() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var amount = new BigDecimal("40.00");
        var operationId = "initiated-recent";
        var now = LocalDateTime.now();

        transferOperationRepository.save(new TransferOperation(
                UUID.randomUUID(),
                operationId,
                UUID.randomUUID(),
                sourceWalletId,
                destinationWalletId,
                amount,
                "payload-recent",
                TransferOperationStatus.INITIATED,
                "recent transfer",
                now,
                now
        ));

        var operation = transferOperationRepository.findByOperationId(operationId).orElseThrow();
        assertFalse(reconciliationService.reconcileOperation(operation));
        assertEquals(TransferOperationStatus.INITIATED,
                transferOperationRepository.findByOperationId(operationId).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Should recover a stale initiated operation to unknown commit state without re-executing money movement")
    void shouldRecoverStaleInitiatedOperationAsUnknownCommitResult() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var amount = new BigDecimal("40.00");
        var operationId = "initiated-stale";
        var now = LocalDateTime.now().minusMinutes(10);

        transferOperationRepository.save(new TransferOperation(
                UUID.randomUUID(),
                operationId,
                UUID.randomUUID(),
                sourceWalletId,
                destinationWalletId,
                amount,
                "payload-stale",
                TransferOperationStatus.INITIATED,
                "stale transfer",
                now,
                now
        ));

        var operation = transferOperationRepository.findByOperationId(operationId).orElseThrow();
        assertFalse(reconciliationService.reconcileOperation(operation));
        assertEquals(TransferOperationStatus.UNKNOWN_COMMIT_RESULT,
                transferOperationRepository.findByOperationId(operationId).orElseThrow().getStatus());
        assertTrue(transactionDataRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Should keep an operation in unknown state when no financial evidence exists")
    void shouldKeepUnknownStatusWhenNoFinancialEvidenceExists() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var amount = new BigDecimal("40.00");
        var operationId = "reconcile-unknown";
        var now = LocalDateTime.now();

        transferOperationRepository.save(new TransferOperation(
                UUID.randomUUID(),
                operationId,
                UUID.randomUUID(),
                sourceWalletId,
                destinationWalletId,
                amount,
                "payload-hash",
                TransferOperationStatus.UNKNOWN_COMMIT_RESULT,
                "indeterminate transfer",
                now,
                now
        ));

        var operation = transferOperationRepository.findByOperationId(operationId).orElseThrow();
        assertFalse(reconciliationService.reconcileOperation(operation));
        assertEquals(TransferOperationStatus.UNKNOWN_COMMIT_RESULT,
                transferOperationRepository.findByOperationId(operationId).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Should not reconcile a transfer using financial evidence from another transfer")
    void shouldNotReconcileATransferUsingEvidenceFromAnotherTransfer() {
        var now = LocalDateTime.now();
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var amount = new BigDecimal("100.00");
        var description = "shared transfer description";

        var appliedTransferId = UUID.randomUUID();
        var unknownTransferId = UUID.randomUUID();

        transferOperationDataRepository.save(new TransferOperationDocument(
                UUID.randomUUID().toString(),
                "operation-applied",
                appliedTransferId.toString(),
                sourceWalletId.toString(),
                destinationWalletId.toString(),
                amount,
                "payload-a",
                TransferOperationStatus.APPLIED.name(),
                description,
                now,
                now
        ));

        transferOperationDataRepository.save(new TransferOperationDocument(
                UUID.randomUUID().toString(),
                "operation-unknown",
                unknownTransferId.toString(),
                sourceWalletId.toString(),
                destinationWalletId.toString(),
                amount,
                "payload-b",
                TransferOperationStatus.UNKNOWN_COMMIT_RESULT.name(),
                description,
                now,
                now
        ));

        transactionDataRepository.save(new io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransactionDocument(
                UUID.randomUUID().toString(),
                sourceWalletId.toString(),
                appliedTransferId.toString(),
                "operation-applied",
                "TRANSFER_OUT",
                amount,
                new BigDecimal("900.00"),
                description,
                now
        ));
        transactionDataRepository.save(new io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransactionDocument(
                UUID.randomUUID().toString(),
                destinationWalletId.toString(),
                appliedTransferId.toString(),
                "operation-applied",
                "TRANSFER_IN",
                amount,
                new BigDecimal("1100.00"),
                description,
                now
        ));

        var unknownOperation = transferOperationRepository.findByOperationId("operation-unknown").orElseThrow();
        assertFalse(reconciliationService.reconcileOperation(unknownOperation));
        assertEquals(TransferOperationStatus.UNKNOWN_COMMIT_RESULT,
                transferOperationRepository.findByOperationId("operation-unknown").orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Should recover stale reconciliation lease and prevent stale workers from finalizing")
    void shouldRecoverExpiredReconciliationLease() {
        var operationId = "reconcile-expired-lease";
        var now = LocalDateTime.now();
        var transferId = UUID.randomUUID();

        transferOperationRepository.save(new TransferOperation(
                UUID.randomUUID(),
                operationId,
                transferId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("15.00"),
                "payload-expired",
                TransferOperationStatus.UNKNOWN_COMMIT_RESULT,
                "lease recovery",
                now,
                now
        ));

        assertTrue(transferOperationRepository.acquireReconciliationLease(operationId, "owner-a", now.minusMinutes(5)));
        assertTrue(transferOperationRepository.acquireReconciliationLease(operationId, "owner-b", now.plusMinutes(5)));
        assertFalse(transferOperationRepository.finalizeReconciliation(operationId, TransferOperationStatus.APPLIED, "owner-a"));
        assertEquals(TransferOperationStatus.RECONCILING,
                transferOperationRepository.findByOperationId(operationId).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Should keep initiated operation pending and never report it as completed")
    void shouldKeepInitiatedOperationPendingAndNeverReportCompleted() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var operationId = "initiated-pending";
        var amount = new BigDecimal("25.00");
        var description = "pending transfer";
        var payloadHash = buildPayloadHash(sourceWalletId, destinationWalletId, amount, description);

        walletRepository.save(new Wallet(sourceWalletId, "John Doe", "john@example.com", new BigDecimal("200.00"), now, now));
        walletRepository.save(new Wallet(destinationWalletId, "Jane Doe", "jane@example.com", new BigDecimal("50.00"), now, now));

        transferOperationRepository.save(new TransferOperation(
                UUID.randomUUID(),
                operationId,
                UUID.randomUUID(),
                sourceWalletId,
                destinationWalletId,
                amount,
                payloadHash,
                TransferOperationStatus.INITIATED,
                description,
                now,
                now
        ));

        var response = transferUseCase.execute(new TransferRequest(
                sourceWalletId,
                destinationWalletId,
                amount,
                description,
                operationId
        ));

        assertEquals("INITIATED", response.getStatus());
        assertNotEquals("COMPLETED", response.getStatus());
        assertEquals(new BigDecimal("200.00"), walletRepository.findById(sourceWalletId).orElseThrow().getBalance());
        assertEquals(new BigDecimal("50.00"), walletRepository.findById(destinationWalletId).orElseThrow().getBalance());
        assertTrue(transactionDataRepository.findByWalletId(sourceWalletId.toString()).isEmpty());
        assertTrue(transactionDataRepository.findByWalletId(destinationWalletId.toString()).isEmpty());
    }

    @Test
    @DisplayName("Should rollback if the APPLIED transition is not persisted in the same transaction")
    void shouldRollbackWhenAppliedTransitionFails() {
        var sourceWalletId = UUID.randomUUID();
        var destinationWalletId = UUID.randomUUID();
        var now = LocalDateTime.now();
        var operationId = "applied-transition-fails";
        var amount = new BigDecimal("35.00");

        walletRepository.save(new Wallet(sourceWalletId, "John Doe", "john@example.com", new BigDecimal("200.00"), now, now));
        walletRepository.save(new Wallet(destinationWalletId, "Jane Doe", "jane@example.com", new BigDecimal("50.00"), now, now));

        doReturn(false).when(transferOperationRepositorySpy)
                .updateStatus(anyString(), eq(TransferOperationStatus.APPLIED));

        var ex = assertThrows(IllegalStateException.class, () -> transferUseCase.execute(new TransferRequest(
                sourceWalletId,
                destinationWalletId,
                amount,
                "applied transition fail",
                operationId
        )));

        assertTrue(ex.getMessage().contains("APPLIED"));
        assertEquals(new BigDecimal("200.00"), walletRepository.findById(sourceWalletId).orElseThrow().getBalance());
        assertEquals(new BigDecimal("50.00"), walletRepository.findById(destinationWalletId).orElseThrow().getBalance());
        assertTrue(transactionDataRepository.findAll().isEmpty());
        assertEquals(TransferOperationStatus.INITIATED,
                transferOperationRepository.findByOperationId(operationId).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Should reject invalid terminal-state overwrite after a terminal decision")
    void shouldRejectInvalidTerminalStateOverwrite() {
        var operationId = "terminal-overwrite";
        var now = LocalDateTime.now();
        var transferId = UUID.randomUUID();

        transferOperationRepository.save(new TransferOperation(
                UUID.randomUUID(),
                operationId,
                transferId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("30.00"),
                "hash-terminal",
                TransferOperationStatus.APPLIED,
                "terminal transfer",
                now,
                now
        ));

        assertFalse(transferOperationRepository.updateStatus(operationId, TransferOperationStatus.UNKNOWN_COMMIT_RESULT));
        assertEquals(TransferOperationStatus.APPLIED,
                transferOperationRepository.findByOperationId(operationId).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Should classify only Mongo unknown commit label as indeterminate")
    void shouldClassifyOnlyMongoUnknownCommitLabelAsIndeterminate() throws Exception {
        var method = TransferUseCase.class.getDeclaredMethod("isUnknownCommitResult", Throwable.class);
        method.setAccessible(true);

        var unknownCommit = new MongoException("commit uncertain");
        unknownCommit.addLabel("UnknownTransactionCommitResult");
        assertTrue((Boolean) method.invoke(transferUseCase, unknownCommit));

        var ordinaryMongoFailure = new MongoException("commit failed before confirmation");
        assertFalse((Boolean) method.invoke(transferUseCase, ordinaryMongoFailure));
    }

    @Test
    @DisplayName("Should persist the unknown commit status for operation reconciliation")
    void shouldPersistUnknownCommitResultStatusForOperationReconciliation() {
        var operationId = "unknown-commit";
        var now = LocalDateTime.now();

        transferOperationDataRepository.save(new TransferOperationDocument(
                UUID.randomUUID().toString(),
                operationId,
                "payload-hash",
                TransferOperationStatus.UNKNOWN_COMMIT_RESULT.name(),
                "reconciliation required",
                now,
                now
        ));

        var saved = transferOperationDataRepository.findByOperationId(operationId).orElseThrow();
        assertEquals("UNKNOWN_COMMIT_RESULT", saved.getStatus());
    }

    private String buildPayloadHash(UUID sourceWalletId, UUID destinationWalletId, BigDecimal amount, String description) {
        String payload = sourceWalletId + "|" + destinationWalletId + "|"
                + amount.stripTrailingZeros().toPlainString() + "|" + description;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
