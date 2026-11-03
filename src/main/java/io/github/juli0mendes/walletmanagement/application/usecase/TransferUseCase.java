package io.github.juli0mendes.walletmanagement.application.usecase;

import com.mongodb.MongoException;
import io.github.juli0mendes.walletmanagement.domain.exception.DuplicateTransferOperationException;
import io.github.juli0mendes.walletmanagement.domain.exception.UnknownTransactionCommitResultException;
import io.github.juli0mendes.walletmanagement.domain.model.Transaction;
import io.github.juli0mendes.walletmanagement.domain.model.TransactionType;
import io.github.juli0mendes.walletmanagement.domain.model.TransferOperation;
import io.github.juli0mendes.walletmanagement.domain.model.TransferOperationStatus;
import io.github.juli0mendes.walletmanagement.domain.model.Wallet;
import io.github.juli0mendes.walletmanagement.domain.repository.TransactionRepository;
import io.github.juli0mendes.walletmanagement.domain.repository.TransferOperationRepository;
import io.github.juli0mendes.walletmanagement.domain.repository.WalletRepository;
import io.github.juli0mendes.walletmanagement.dto.TransferRequest;
import io.github.juli0mendes.walletmanagement.dto.TransferResponse;
import io.github.juli0mendes.walletmanagement.exception.WalletNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class TransferUseCase {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransferOperationRepository transferOperationRepository;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    @Value("${wallet.transfer.reconciliation.lease-duration:PT5M}")
    private Duration executionLeaseDuration = Duration.ofMinutes(5);

    public TransferUseCase(WalletRepository walletRepository,
                           TransactionRepository transactionRepository,
                           TransferOperationRepository transferOperationRepository) {
        this(walletRepository, transactionRepository, transferOperationRepository, Clock.systemDefaultZone(), null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public TransferUseCase(WalletRepository walletRepository,
                           TransactionRepository transactionRepository,
                           TransferOperationRepository transferOperationRepository,
                           Clock clock,
                           MongoTransactionManager mongoTransactionManager) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.transferOperationRepository = transferOperationRepository;
        this.clock = clock == null ? Clock.systemDefaultZone() : clock;
        this.transactionTemplate = mongoTransactionManager == null ? null : new TransactionTemplate(mongoTransactionManager);
    }

    public TransferResponse execute(TransferRequest request) {
        String operationId = request.operationId() == null || request.operationId().isBlank()
                ? UUID.randomUUID().toString()
                : request.operationId();
        String payloadHash = buildPayloadHash(request);
        Optional<TransferOperation> existingOperation = transferOperationRepository.findByOperationId(operationId);

        if (existingOperation.isPresent()) {
            TransferOperation operation = existingOperation.get();
            if (!operation.getPayloadHash().equals(payloadHash)) {
                throw new DuplicateTransferOperationException(
                        "Transfer operation id already exists with a different payload"
                );
            }
            return toResponse(operation);
        }

        UUID transferId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now(clock);
        TransferOperation operation = new TransferOperation(
                UUID.randomUUID(),
                operationId,
                transferId,
                request.sourceWalletId(),
                request.destinationWalletId(),
                request.amount(),
                payloadHash,
                TransferOperationStatus.INITIATED,
                request.description(),
                now,
                now
        );

        try {
            transferOperationRepository.save(operation);
        } catch (DataIntegrityViolationException ex) {
            Optional<TransferOperation> concurrent = transferOperationRepository.findByOperationId(operationId);
            if (concurrent.isPresent() && concurrent.get().getPayloadHash().equals(payloadHash)) {
                return toResponse(concurrent.get());
            }
            throw ex;
        }

        String executionOwner = buildExecutionOwner(operationId);
        LocalDateTime executionLeaseExpiresAt = LocalDateTime.now(clock).plus(executionLeaseDuration);
        if (!transferOperationRepository.markExecutionInProgress(operationId, executionOwner, executionLeaseExpiresAt)) {
            throw new IllegalStateException("Transfer execution lease could not be acquired for operation id: " + operationId);
        }

        try {
            if (transactionTemplate == null) {
                return executeTransactionalLogic(request, operationId, payloadHash, transferId, now, executionOwner);
            }
            return transactionTemplate.execute(status -> executeTransactionalLogic(request, operationId, payloadHash, transferId, now, executionOwner));
        } catch (RuntimeException ex) {
            if (isUnknownCommitResult(ex)) {
                transferOperationRepository.updateStatus(operationId, TransferOperationStatus.UNKNOWN_COMMIT_RESULT);
                throw new UnknownTransactionCommitResultException(
                        "Transfer commit result is unknown for operation id: " + operationId
                );
            }
            throw ex;
        }
    }

    private TransferResponse executeTransactionalLogic(TransferRequest request,
                                                     String operationId,
                                                     String payloadHash,
                                                     UUID transferId,
                                                     LocalDateTime now,
                                                     String executionOwner) {
        LocalDateTime executionNow = LocalDateTime.now(clock);
        UUID sourceWalletId = request.sourceWalletId();
        UUID destinationWalletId = request.destinationWalletId();
        BigDecimal amount = request.amount();

        Wallet sourceWallet = walletRepository.findById(sourceWalletId)
                .orElseThrow(() -> new WalletNotFoundException(sourceWalletId.toString()));

        Wallet destinationWallet = walletRepository.findById(destinationWalletId)
                .orElseThrow(() -> new WalletNotFoundException(destinationWalletId.toString()));

        sourceWallet.withdraw(amount);
        destinationWallet.deposit(amount);

        walletRepository.save(sourceWallet);
        walletRepository.save(destinationWallet);

        Transaction sourceTransaction = new Transaction(
                UUID.randomUUID(),
                sourceWalletId,
                TransactionType.TRANSFER_OUT,
                amount,
                sourceWallet.getBalance(),
                request.description(),
                transferId,
                operationId,
                now
        );
        transactionRepository.save(sourceTransaction);

        Transaction destTransaction = new Transaction(
                UUID.randomUUID(),
                destinationWalletId,
                TransactionType.TRANSFER_IN,
                amount,
                destinationWallet.getBalance(),
                request.description(),
                transferId,
                operationId,
                now
        );
        transactionRepository.save(destTransaction);

        if (!transferOperationRepository.updateStatus(operationId, TransferOperationStatus.APPLIED, executionOwner)) {
            throw new IllegalStateException("Transfer operation transition to APPLIED failed for operation id: " + operationId);
        }

        return new TransferResponse(
                transferId,
                operationId,
                sourceWalletId,
                destinationWalletId,
                amount,
                "COMPLETED",
                request.description(),
                now
        );
    }

    private String buildExecutionOwner(String operationId) {
        String prefix = "transfer-execution:" + operationId + ":";
        String currentOwner = transferOperationRepository.findByOperationId(operationId)
                .map(TransferOperation::getLeaseOwner)
                .orElse(null);
        long nextFenceToken = 1L;

        if (currentOwner != null && currentOwner.startsWith(prefix)) {
            String tokenValue = currentOwner.substring(prefix.length());
            try {
                nextFenceToken = Long.parseLong(tokenValue) + 1L;
            } catch (NumberFormatException ignored) {
                nextFenceToken = 1L;
            }
        }

        return prefix + nextFenceToken;
    }

    private TransferResponse toResponse(TransferOperation operation) {
        String responseStatus = switch (operation.getStatus()) {
            case APPLIED -> "COMPLETED";
            case INITIATED -> "INITIATED";
            case UNKNOWN_COMMIT_RESULT -> "UNKNOWN_COMMIT_RESULT";
            case REJECTED -> "REJECTED";
            case RECONCILING -> "RECONCILING";
        };

        return new TransferResponse(
                operation.getTransferId(),
                operation.getOperationId(),
                operation.getSourceWalletId(),
                operation.getDestinationWalletId(),
                operation.getAmount(),
                responseStatus,
                operation.getDescription(),
                operation.getUpdatedAt()
        );
    }

    private boolean isUnknownCommitResult(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof MongoException mongoException && mongoException.hasErrorLabel("UnknownTransactionCommitResult")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String buildPayloadHash(TransferRequest request) {
        String payload = request.sourceWalletId() + "|" + request.destinationWalletId() + "|"
                + request.amount().stripTrailingZeros().toPlainString() + "|" + request.description();
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
