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
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(AopUtils.isAopProxy(transferUseCase));

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
                "TRANSFER_OUT",
                amount,
                new BigDecimal("160.00"),
                description,
                now
        ));
        transactionDataRepository.save(new io.github.juli0mendes.walletmanagement.infrastructure.persistence.document.TransactionDocument(
                UUID.randomUUID().toString(),
                destinationWalletId.toString(),
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
}
